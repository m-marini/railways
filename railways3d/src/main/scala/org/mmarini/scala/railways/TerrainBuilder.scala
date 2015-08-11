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
import com.jme3.terrain.heightmap.ImageBasedHeightMap

/**
 *
 */
object TerrainBuilder extends LazyLogging {

  /** Loads terrains */
  def build(assetManager: AssetManager, camera: Camera): Try[Node] = {
    val ty = for {
      mat <- loadTerrainMaterial(assetManager)
      map <- loadHightMap(assetManager)
    } yield {
      val PatchSize = 65
      val QuadSize = 513
      val terrain = new TerrainQuad("my terrain", PatchSize, QuadSize, map)
      terrain.setMaterial(loadTerrainMaterial(assetManager).get)
      terrain.setLocalScale(4f, 0.5f, 4f)
//      terrain.setLocalTranslation(0f, -256f, 0f)

      /** 5. The LOD (level of detail) depends on were the camera is: */
      terrain.addControl(new TerrainLodControl(terrain, camera))

      terrain
    }
    ty.failed.foreach(ex => logger.error(ex.getMessage(), ex))
    ty
  }

  /** Loads the height map */
  private def loadHightMap(assetManager: AssetManager) =
    Try {
      val heightMapImage = assetManager.loadTexture("Textures/himap.png")
      val heightmap = new ImageBasedHeightMap(heightMapImage.getImage)
      heightmap.load()
      heightmap.getHeightMap
    }

  /** Load terrain material */
  private def loadTerrainMaterial(assetManager: AssetManager) =
    Try {
      val mat_terrain = new Material(assetManager, "Common/MatDefs/Terrain/Terrain.j3md")

      val alpha = assetManager.loadTexture("Textures/alphamap.png")
      alpha.setWrap(WrapMode.Repeat)
      mat_terrain.setTexture("Alpha", alpha)

      val dirt = assetManager.loadTexture("Textures/dirt.jpg")
      dirt.setWrap(WrapMode.Repeat)
      mat_terrain.setTexture("Tex2", dirt)
      mat_terrain.setFloat("Tex2Scale", 3f)

      val grass = assetManager.loadTexture("Textures/grass.jpg")
      grass.setWrap(WrapMode.Repeat)
      mat_terrain.setTexture("Tex1", grass)
      mat_terrain.setFloat("Tex1Scale", 1000f)

      val snow = assetManager.loadTexture("Textures/snow.jpg")
      snow.setWrap(WrapMode.Repeat)
      mat_terrain.setTexture("Tex3", snow)
      mat_terrain.setFloat("Tex3Scale", 1f)

      mat_terrain
    }

}
