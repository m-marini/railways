/*
 * Copyright (c) 2023  Marco Marini, marco.marini@mmarini.org
 *
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following
 * conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 *
 *    END OF TERMS AND CONDITIONS
 *
 */

package org.mmarini.railways2.model.blocks;

import com.fasterxml.jackson.databind.JsonNode;
import org.mmarini.LazyValue;
import org.mmarini.Tuple2;
import org.mmarini.yaml.Utils;
import org.mmarini.yaml.schema.Locator;
import org.mmarini.yaml.schema.Validator;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static org.mmarini.yaml.Utils.DYNAMIC_OBJECT;
import static org.mmarini.yaml.schema.Validator.*;

/**
 * The station definitions from json
 */
public class StationDef {
    private static final Validator BLOCKS_VALIDATOR = objectAdditionalProperties(DYNAMIC_OBJECT);
    private static final Validator LINKS_VALIDATOR = objectAdditionalProperties(string());
    public final static Validator VALIDATOR = objectPropertiesRequired(Map.of(
                    "name", string(),
                    "orientation", integer(minimum(0), maximum(359)),
                    "blocks", BLOCKS_VALIDATOR,
                    "links", LINKS_VALIDATOR
            ),
            List.of("name", "orientation", "blocks", "links")
    );

    /**
     * Returns the station definition from json node
     *
     * @param root    the json document
     * @param locator the locator of station definition
     */
    public static StationDef create(JsonNode root, Locator locator) {
        VALIDATOR.apply(locator).accept(root);
        String id = locator.path("name").getNode(root).asText();
        int orientation = locator.path("orientation").getNode(root).asInt();
        List<Block> blocks1 = locator.path("blocks").propertyNames(root)
                .map(t -> Utils.<Block>createObject(root, t._2,
                        new Object[]{t._1}, new Class[]{String.class}))
                .collect(Collectors.toList());
        List<Tuple2<String, String>> links1 = locator.path("links").propertyNames(root)
                .map(t -> t.setV2(t._2.getNode(root).asText()))
                .collect(Collectors.toList());
        return new StationDef(id, orientation, blocks1, links1);
    }

    /**
     * Creates the station definition
     *
     * @param id          the identifier
     * @param orientation the orientation
     * @param blocks      the blocks
     * @param links       the links
     */
    public static StationDef create(String id, int orientation, Collection<? extends Block> blocks, Map<String, String> links) {
        // Normalizes the links
        Set<Tuple2<String, String>> normalLinks = Tuple2.stream(links)
                .map(t -> t._1.compareTo(t._2) <= 0 ? t : Tuple2.of(t._2, t._1)
                ).collect(Collectors.toSet());
        // Validates
        List<Tuple2<String, Integer>> duplicated = Tuple2.stream(normalLinks.stream()
                        .collect(Collectors.groupingBy(Tuple2::getV1)))
                .map(t -> t.setV2(t._2.size()))
                .filter(t -> t._2 > 1)
                .collect(Collectors.toList());
        if (!duplicated.isEmpty()) {
            StringJoiner list = new StringJoiner(",", "[", "]");
            duplicated.forEach(t -> list.add(t._1));
            throw new IllegalArgumentException(format("Duplicated links %s", list));
        }
        return new StationDef(id, orientation, blocks, normalLinks);
    }

    private final String id;
    private final int orientation;
    private final Collection<? extends Block> blocks;
    private final Collection<Tuple2<String, String>> links;
    private final LazyValue<Collection<BlockPoint>> blockPoints;
    private final LazyValue<Collection<BlockJunction>> declaredJunctions;
    private final LazyValue<Map<String, ? extends Block>> blocksById;
    private final LazyValue<Map<Block, List<BlockJunction>>> junctionsByBlock;

    /**
     * Creates the station definition
     *
     * @param id          the identifier
     * @param orientation the orientation
     * @param blocks      the blocks
     * @param links       the links
     */
    protected StationDef(String id, int orientation, Collection<? extends Block> blocks, Collection<Tuple2<String, String>> links) {
        this.id = requireNonNull(id);
        this.orientation = orientation;
        this.blocks = requireNonNull(blocks);
        this.links = requireNonNull(links);
        this.blockPoints = new LazyValue<>(this::createBlockPoints);
        this.declaredJunctions = new LazyValue<>(this::createDeclaredJunctions);
        this.blocksById = new LazyValue<>(() -> blocks.stream().collect(Collectors.toMap(
                Block::getId,
                Function.identity())));
        this.junctionsByBlock = new LazyValue<>(this::createJunctionsByBlock);
    }

    /**
     * Returns the block points
     */
    private Collection<BlockPoint> createBlockPoints() {
        return blocks.stream()
                .flatMap(b -> b.getBlockPoints().stream())
                .collect(Collectors.toList());
    }

    /**
     * Returns the declared junctions
     */
    private Collection<BlockJunction> createDeclaredJunctions() {
        return links.stream().map(t ->
                        BlockJunction.create(decodeConnection(t._1), decodeConnection(t._2)))
                .collect(Collectors.toList());
    }

    /**
     * Returns the junctions by block
     */
    private Map<Block, List<BlockJunction>> createJunctionsByBlock() {
        Map<Block, List<Tuple2<Block, BlockJunction>>> groupBy = getDeclaredJunctions().stream()
                .flatMap(j -> Stream.of(
                        Tuple2.of(j.getFrom().<Block>getBlock(), j),
                        Tuple2.of(j.getTo().<Block>getBlock(), j)
                ))
                .collect(Collectors.groupingBy(Tuple2::getV1));
        // Creates the junction by block
        return Tuple2.stream(groupBy)
                .map(t -> t.setV2(
                        t._2.stream()
                                .map(Tuple2::getV2)
                                .collect(Collectors.toList())
                ))
                .collect(Tuple2.toMap());
    }

    /**
     * Returns the block and blockConnectionId from the full connection id
     *
     * @param id the full connection id
     */
    BlockPoint decodeConnection(String id) {
        int i = id.indexOf(".");
        if (i < 0) {
            throw new IllegalArgumentException(format("Wrong connection id [%s]", id));
        }
        String blockId = id.substring(0, i);
        Block block = getBlocksById().get(blockId);
        if (block == null) {
            throw new IllegalArgumentException(format("Block [%s] not found", blockId));
        }
        String connId = id.substring(i + 1);
        // Validates the connection id
        block.getEntryGeometry(connId);
        return new BlockPoint(block, connId);
    }

    /**
     * Returns the block
     *
     * @param id  the id
     * @param <T> the block type
     */
    public <T extends Block> Optional<T> getBlock(String id) {
        return (Optional<T>) Optional.ofNullable(getBlocksById().get(id));
    }

    /**
     * Returns the block points
     */
    public Collection<BlockPoint> getBlockPoints() {
        return blockPoints.get();
    }

    /**
     * Returns the blocks
     */
    public Collection<? extends Block> getBlocks() {
        return blocks;
    }

    /**
     * Returns the block by id map
     */
    public Map<String, ? extends Block> getBlocksById() {
        return blocksById.get();
    }

    /**
     * Returns the declared junctions (lazy value)
     */
    public Collection<BlockJunction> getDeclaredJunctions() {
        return declaredJunctions.get();
    }

    /**
     * Returns the station identifier
     */
    public String getId() {
        return id;
    }

    /**
     * Returns the list of junctions for the block
     *
     * @param block the block
     */
    List<BlockJunction> getJunctions(Block block) {
        return getJunctionsByBlock().get(block);
    }

    /**
     * Returns the junction by block
     */
    private Map<Block, List<BlockJunction>> getJunctionsByBlock() {
        return junctionsByBlock.get();
    }

    /**
     * Returns the links
     */
    public Collection<Tuple2<String, String>> getLinks() {
        return links;
    }

    /**
     * Returns the orientation
     */
    public int getOrientation() {
        return orientation;
    }
}
