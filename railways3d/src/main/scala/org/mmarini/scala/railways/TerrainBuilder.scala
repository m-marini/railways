/**
 *
 */
package org.mmarini.scala.railways

import com.jme3.terrain.geomipmap.TerrainLodControl
import com.jme3.material.Material
import com.jme3.terrain.geomipmap.TerrainQuad
import scala.util.Try
import com.jme3.asset.AssetManager
import com.jme3.renderer.Camera
import com.jme3.scene.Spatial
import com.jme3.texture.Texture.WrapMode
import com.jme3.scene.Node
import com.typesafe.scalalogging.LazyLogging
import scala.util.Random

/**
 *
 */
object TerrainBuilder extends LazyLogging {

  /** Loads terrains */
  def build(assetManager: AssetManager, camera: Camera): Try[Node] = {
    val ty = for {
      mat <- loadTerrainMaterial(assetManager)
      map <- loadHightMap
    } yield {
      val PatchSize = 65
      val QuadSize = 513
      val terrain = new TerrainQuad("my terrain", PatchSize, QuadSize, map)
      terrain.setMaterial(loadTerrainMaterial(assetManager).get)
      terrain.setLocalScale(1f, 1f, 1f)

      /** 5. The LOD (level of detail) depends on were the camera is: */
      terrain.addControl(new TerrainLodControl(terrain, camera))

      terrain
    }
    ty.failed.foreach(ex => logger.error(ex.getMessage(), ex))
    ty
  }

  /** Loads the height map */
  private def loadHightMap =
    Try {
      //      val heightMapImage = app.getAssetManager.loadTexture("Textures/station-terrain-height.png")
      //      val heightmap = new ImageBasedHeightMap(heightMapImage.getImage)
      //      heightmap.load()
      //      heightmap.getHeightMap
      val map = new Array[Float](512 * 512)
      map
    }

  /** Load terrain material */
  private def loadTerrainMaterial(assetManager: AssetManager) =
    Try {
      val mat_terrain = new Material(assetManager, "Common/MatDefs/Terrain/Terrain.j3md")

      val alpha = assetManager.loadTexture("Textures/station-terrain-alpha.png")
      alpha.setWrap(WrapMode.Repeat)
      mat_terrain.setTexture("Alpha", alpha)

      val dirt = assetManager.loadTexture("Textures/dirt.jpg")
      dirt.setWrap(WrapMode.Repeat)
      mat_terrain.setTexture("Tex2", dirt)
      mat_terrain.setFloat("Tex2Scale", 32f)

      val grass = assetManager.loadTexture("Textures/grass.jpg")
      grass.setWrap(WrapMode.Repeat)
      mat_terrain.setTexture("Tex1", grass)
      mat_terrain.setFloat("Tex2Scale", 64f)
      mat_terrain
    }

}
