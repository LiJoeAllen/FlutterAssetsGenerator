package com.crzsc.plugin.test

import com.crzsc.plugin.utils.DartClassGenerator
import com.crzsc.plugin.utils.AssetNode
import com.crzsc.plugin.utils.MediaType
import com.crzsc.plugin.utils.ModulePubSpecConfig
import com.crzsc.plugin.utils.SemanticVersion
import com.intellij.openapi.module.Module
import io.flutter.pub.PubRoot
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.Mockito.mock

/**
 * 测试 DartClassGenerator 生成的代码结构
 */
class DartClassGeneratorTest {
    
    @Test
    fun testNoDuplicateClasses() {
        // 构建测试资源树
        val root = AssetNode("Assets", "", MediaType.DIRECTORY, null)
        val assetsDir = AssetNode("assets", "assets", MediaType.DIRECTORY, null)
        root.children.add(assetsDir)
        
        // 添加子目录
        val imageDir = AssetNode("image", "assets/image", MediaType.DIRECTORY, null)
        val svgDir = AssetNode("svg", "assets/svg", MediaType.DIRECTORY, null)
        val lottieDir = AssetNode("lottie", "assets/lottie", MediaType.DIRECTORY, null)
        
        assetsDir.children.add(imageDir)
        assetsDir.children.add(svgDir)
        assetsDir.children.add(lottieDir)
        
        // 添加文件
        imageDir.children.add(AssetNode("test.png", "assets/image/test.png", MediaType.IMAGE, null))
        svgDir.children.add(AssetNode("test.svg", "assets/svg/test.svg", MediaType.SVG, null))
        lottieDir.children.add(AssetNode("test.json", "assets/lottie/test.json", MediaType.LOTTIE, null))
        
        // 生成代码
        val config = createMockConfig()
        val generator = DartClassGenerator(
            root,
            config,
            hasSvg = true,
            hasLottie = true,
            flutterVersion = SemanticVersion(3, 0, 0)
        )
        val generatedCode = generator.generate()
        
        // 验证:检查类定义数量
        val imageGenCount = generatedCode.split("class \$AssetsImageGen").size - 1
        val svgGenCount = generatedCode.split("class \$AssetsSvgGen").size - 1
        val lottieGenCount = generatedCode.split("class \$AssetsLottieGen").size - 1
        val assetsGenCount = generatedCode.split("class \$AssetsAssetsGen").size - 1
        
        // 每个类应该只出现一次
        assertEquals("$AssetsImageGen should appear exactly once", 1, imageGenCount)
        assertEquals("$AssetsSvgGen should appear exactly once", 1, svgGenCount)
        assertEquals("$AssetsLottieGen should appear exactly once", 1, lottieGenCount)
        assertEquals("$AssetsAssetsGen should not appear", 0, assetsGenCount)
        
        // 验证:根类应该包含三个字段
        assertTrue("Root class should contain image field", generatedCode.contains("static const \$AssetsImageGen image"))
        assertTrue("Root class should contain svg field", generatedCode.contains("static const \$AssetsSvgGen svg"))
        assertTrue("Root class should contain lottie field", generatedCode.contains("static const \$AssetsLottieGen lottie"))
        assertTrue("Generated code should contain root class name", generatedCode.contains("class Assets"))
        assertTrue("Generated code should include Lottie import", generatedCode.contains("package:lottie/lottie.dart"))
        assertTrue("Generated code should include SVG import", generatedCode.contains("package:flutter_svg/flutter_svg.dart"))
        
        println("Generated code structure is correct!")
        println(generatedCode)
    }
    
    private fun createMockConfig(): ModulePubSpecConfig {
        val module = mock(Module::class.java)
        val pubRoot = mock(PubRoot::class.java)
        val map = mapOf(
            "name" to "test_app",
            "flutter_assets_generator" to mapOf(
                "class_name" to "Assets",
                "style" to "robust",
                "leading_with_package_name" to false
            )
        )
        return ModulePubSpecConfig(
            module = module,
            pubRoot = pubRoot,
            assetVFiles = emptyList(),
            map = map,
            isFlutterModule = false
        )
    }
}
