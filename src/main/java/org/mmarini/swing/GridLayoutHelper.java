/**
 *
 */
package org.mmarini.swing;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.function.BiFunction;

import static java.lang.String.format;

/**
 * @author us00852
 */
public class GridLayoutHelper<T extends Container> {

    private static final Logger logger = LoggerFactory
            .getLogger(GridLayoutHelper.class);
    private static final Map<String, Modifier> MODIFIERS = new HashMap<>();

    static {
        MODIFIERS.put("at", (c, args) -> {
            if (args.length >= 3)
                try {
                    final GridBagConstraints n = create(c);
                    n.gridx = Integer.parseInt(args[1]);
                    n.gridy = Integer.parseInt(args[2]);
                    return n;
                } catch (final NumberFormatException e) {
                    throw new IllegalArgumentException(e);
                }
            return c;
        });
        MODIFIERS.put("ipad", (c, args) -> {
            if (args.length >= 3)
                try {
                    final GridBagConstraints n = create(c);
                    n.ipadx = Integer.parseInt(args[1]);
                    n.ipady = Integer.parseInt(args[2]);
                    return n;
                } catch (final NumberFormatException e) {
                    throw new IllegalArgumentException(e);
                }
            return c;
        });
        MODIFIERS.put("span", (c, args) -> {
            if (args.length >= 3)
                try {
                    final GridBagConstraints n = create(c);
                    n.gridwidth = Integer.parseInt(args[1]);
                    n.gridheight = Integer.parseInt(args[2]);
                    return n;
                } catch (final NumberFormatException e) {
                    throw new IllegalArgumentException(e);
                }
            return c;
        });
        MODIFIERS.put("insets", (c, args) -> {
            if (args.length <= 1)
                return c;
            try {
                final int t = Integer.parseInt(args[1]);
                int l = t;
                int b = t;
                int r = t;
                if (args.length >= 3)
                    l = r = Integer.parseInt(args[2]);
                if (args.length >= 5) {
                    b = Integer.parseInt(args[3]);
                    r = Integer.parseInt(args[4]);
                }
                final GridBagConstraints n = create(c);
                n.insets = new Insets(t, l, b, r);
                return n;
            } catch (final NumberFormatException e) {
                throw new IllegalArgumentException(e);
            }
        });
        MODIFIERS.put("weight", (c, args) -> {
            if (args.length >= 3)
                try {
                    final GridBagConstraints n = create(c);
                    n.weightx = Double.parseDouble(args[1]);
                    n.weighty = Double.parseDouble(args[2]);
                    return n;
                } catch (final NumberFormatException e) {
                    throw new IllegalArgumentException(e);
                }
            return c;
        });
        MODIFIERS.put("def", (c, args) -> new GridBagConstraints());
        MODIFIERS.put("e", (c, args) -> {
            final GridBagConstraints n = create(c);
            n.anchor = GridBagConstraints.EAST;
            return n;
        });
        MODIFIERS.put("ne", (c, args) -> {
            final GridBagConstraints n = create(c);
            n.anchor = GridBagConstraints.NORTHEAST;
            return n;
        });
        MODIFIERS.put("nw", (c, args) -> {
            final GridBagConstraints n = create(c);
            n.anchor = GridBagConstraints.NORTHWEST;
            return n;
        });
        MODIFIERS.put("w", (c, args) -> {
            final GridBagConstraints n = create(c);
            n.anchor = GridBagConstraints.WEST;
            return n;
        });
        MODIFIERS.put("n", (c, args) -> {
            final GridBagConstraints n = create(c);
            n.anchor = GridBagConstraints.NORTH;
            return n;
        });
        MODIFIERS.put("s", (c, args) -> {
            final GridBagConstraints n = create(c);
            n.anchor = GridBagConstraints.SOUTH;
            return n;
        });
        MODIFIERS.put("se", (c, args) -> {
            final GridBagConstraints n = create(c);
            n.anchor = GridBagConstraints.SOUTHEAST;
            return n;
        });
        MODIFIERS.put("sw", (c, args) -> {
            final GridBagConstraints n = create(c);
            n.anchor = GridBagConstraints.SOUTHWEST;
            return n;
        });
        MODIFIERS.put("center", (c, args) -> {
            final GridBagConstraints n = create(c);
            n.anchor = GridBagConstraints.CENTER;
            return n;
        });
        MODIFIERS.put("hspan", (c, args) -> {
            final GridBagConstraints n = create(c);
            n.gridwidth = GridBagConstraints.REMAINDER;
            return n;
        });
        MODIFIERS.put("vspan", (c, args) -> {
            final GridBagConstraints n = create(c);
            n.gridheight = GridBagConstraints.REMAINDER;
            return n;
        });
        MODIFIERS.put("nospan", (c, args) -> {
            final GridBagConstraints n = create(c);
            n.gridwidth = 1;
            n.gridheight = 1;
            return n;
        });
        MODIFIERS.put("nofill", (c, args) -> {
            final GridBagConstraints n = create(c);
            n.fill = GridBagConstraints.NONE;
            return n;
        });
        MODIFIERS.put("fill", (c, args) -> {
            final GridBagConstraints n = create(c);
            n.fill = GridBagConstraints.BOTH;
            return n;
        });
        MODIFIERS.put("hfill", (c, args) -> {
            final GridBagConstraints n = create(c);
            n.fill = GridBagConstraints.HORIZONTAL;
            return n;
        });
        MODIFIERS.put("vfill", (c, args) -> {
            final GridBagConstraints n = create(c);
            n.fill = GridBagConstraints.VERTICAL;
            return n;
        });
        MODIFIERS.put("noinsets", (c, args) -> {
            final GridBagConstraints n = create(c);
            n.insets = new Insets(0, 0, 0, 0);
            return n;
        });
        MODIFIERS.put("noweight", (c, args) -> {
            final GridBagConstraints n = create(c);
            n.weightx = 0.0;
            n.weighty = 0.0;
            return n;
        });
        MODIFIERS.put("hw", (c, args) -> {

            if (args.length >= 2)
                try {
                    final GridBagConstraints n = create(c);
                    n.weightx = Double.parseDouble(args[1]);
                    return n;
                } catch (final NumberFormatException e) {
                    throw new IllegalArgumentException(e);
                }
            else {
                final GridBagConstraints n = create(c);
                n.weightx = 1.0;
                return n;
            }
        });
        MODIFIERS.put("vw", (c, args) -> {

            if (args.length >= 2)
                try {
                    final GridBagConstraints n = create(c);
                    n.weighty = Double.parseDouble(args[1]);
                    return n;
                } catch (final NumberFormatException e) {
                    throw new IllegalArgumentException(e);
                }
            else {
                final GridBagConstraints n = create(c);
                n.weighty = 1.0;
                return n;
            }
        });
    }

    /**
     * @param c
     * @return
     */
    private static GridBagConstraints create(final GridBagConstraints c) {
        return new GridBagConstraints(c.gridx, c.gridy, c.gridwidth,
                c.gridheight, c.weightx, c.weighty, c.anchor, c.fill, c.insets,
                c.ipadx, c.ipady);
    }

    public static GridBagConstraints modify(final GridBagConstraints c,
                                            final String modifiers) {
        GridBagConstraints n = c;
        for (final String s : modifiers.split("\\s+")) {
            final String[] a = s.split(",");
            final Modifier m = MODIFIERS.get(a[0]);
            if (m == null) {
                throw new IllegalArgumentException(format("wrong modifier %s", a[0]));
            }
            n = m.apply(n, a);
        }
        return n;
    }

    private final ResourceBundle bundle;
    private final T container;
    private final GridBagLayout layout;
    private GridBagConstraints constraints;

    /**
     * @param bundle
     * @param container
     */
    public GridLayoutHelper(final ResourceBundle bundle, final T container) {
        this.bundle = bundle;
        this.container = container;
        layout = new GridBagLayout();
        constraints = new GridBagConstraints();
        container.setLayout(layout);
    }

    /**
     * @param container
     */
    public GridLayoutHelper(final T container) {
        this(null, container);
    }

    public GridLayoutHelper<T> add(final Component c) {
        layout.setConstraints(c, constraints);
        container.add(c);
        return this;
    }

    public GridLayoutHelper<T> add(final Object... args) {
        GridBagConstraints gbc = constraints;
        for (final Object o : args)
            if (o instanceof Component) {
                final Component c = (Component) o;
                layout.setConstraints(c, gbc);
                container.add(c);
                gbc = constraints;
            } else if (o instanceof Action) {
                final JButton c = new JButton((Action) o);
                layout.setConstraints(c, gbc);
                container.add(c);
                gbc = constraints;
            } else {
                final String s = String.valueOf(o);
                if (s.startsWith("+"))
                    gbc = modify(gbc, s.substring(1));
                else {
                    final JLabel c = new JLabel(getString(s));
                    layout.setConstraints(c, gbc);
                    container.add(c);
                    gbc = constraints;
                }
            }
        return this;
    }

    public GridLayoutHelper<T> at(int x, int y) {
        constraints.gridx = x;
        constraints.gridy = y;
        return this;
    }

    public GridLayoutHelper<T> center() {
        constraints.anchor = GridBagConstraints.CENTER;
        return this;
    }

    public GridLayoutHelper<T> e() {
        constraints.anchor = GridBagConstraints.EAST;
        return this;
    }

    /**
     * @return the container
     */
    public T getContainer() {
        return container;
    }

    /**
     * @param key
     * @return
     */
    private String getString(final String key) {
        if (bundle != null)
            try {
                return bundle.getString(key);
            } catch (final MissingResourceException e) {
                return '!' + key + '!';
            }
        else
            return key;
    }

    public GridLayoutHelper<T> hfill() {
        constraints.fill = GridBagConstraints.HORIZONTAL;
        return this;
    }

    public GridLayoutHelper<T> hspan() {
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        return this;
    }

    public GridLayoutHelper<T> hw() {
        constraints.weightx = 1;
        return this;
    }

    public GridLayoutHelper<T> hw(double weight) {
        constraints.weightx = weight;
        return this;
    }

    public GridLayoutHelper<T> insets(int top, int left, int bottom, int right) {
        constraints.insets = new Insets(top, left, bottom, right);
        return this;
    }

    public GridLayoutHelper<T> insets(int size) {
        return insets(size, size, size, size);
    }

    public GridLayoutHelper<T> insets(int hSize, int vSize) {
        return insets(hSize, vSize, hSize, vSize);
    }

    public GridLayoutHelper<T> ipad(int x, int y) {
        constraints.ipadx = x;
        constraints.ipady = y;
        return this;
    }

    /**
     * @param mods
     * @return
     */
    public GridLayoutHelper<T> modify(final String mods) {
        constraints = modify(constraints, mods);
        return this;
    }

    public GridLayoutHelper<T> n() {
        constraints.anchor = GridBagConstraints.NORTH;
        return this;
    }

    public GridLayoutHelper<T> ne() {
        constraints.anchor = GridBagConstraints.NORTHEAST;
        return this;
    }

    public GridLayoutHelper<T> nofill() {
        constraints.fill = GridBagConstraints.NONE;
        return this;
    }

    public GridLayoutHelper<T> noinsets() {
        constraints.insets = new Insets(0, 0, 0, 0);
        return this;
    }

    public GridLayoutHelper<T> nospan() {
        constraints.gridwidth = 1;
        constraints.gridheight = 1;
        return this;
    }

    public GridLayoutHelper<T> noweight() {
        constraints.weightx = 0.0;
        constraints.weighty = 0.0;
        return this;
    }

    public GridLayoutHelper<T> nw() {
        constraints.anchor = GridBagConstraints.NORTHWEST;
        return this;
    }

    public GridLayoutHelper<T> s() {
        constraints.anchor = GridBagConstraints.SOUTH;
        return this;
    }

    public GridLayoutHelper<T> se() {
        constraints.anchor = GridBagConstraints.SOUTHWEST;
        return this;
    }

    public GridLayoutHelper<T> span(int width, int height) {
        constraints.gridwidth = width;
        constraints.gridheight = height;
        return this;
    }

    public GridLayoutHelper<T> sw() {
        constraints.anchor = GridBagConstraints.SOUTHEAST;
        return this;
    }

    public GridLayoutHelper<T> vfill() {
        constraints.fill = GridBagConstraints.VERTICAL;
        return this;
    }

    public GridLayoutHelper<T> vspan() {
        constraints.gridy = GridBagConstraints.REMAINDER;
        return this;
    }

    public GridLayoutHelper<T> vw() {
        constraints.weighty = 1;
        return this;
    }

    public GridLayoutHelper<T> vw(double weight) {
        constraints.weighty = weight;
        return this;
    }

    public GridLayoutHelper<T> w() {
        constraints.anchor = GridBagConstraints.WEST;
        return this;
    }

    public GridLayoutHelper<T> weight(double x, double y) {
        constraints.weightx = x;
        constraints.weighty = y;
        return this;
    }

    interface Modifier extends
            BiFunction<GridBagConstraints, String[], GridBagConstraints> {
    }
}