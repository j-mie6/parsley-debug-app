import org.scalatest
import org.scalatest.flatspec.AnyFlatSpec   
import org.scalatest.matchers.*

import scala.util.{Try, Success, Failure}

import model.DebugTree
import model.DebugNode

import controller.DebugTreeController

class Test extends AnyFlatSpec with should.Matchers {

    val jsonTree: String = """{
        "input": "Test",
        "root": {
            "nodeId": 0,
            "name": "Test",
            "internal": "Test",
            "success": true,
            "childId": 0,
            "input": "Test",
            "isLeaf": false,
            "doesNeedBubbling": false
        }
    }"""

    val tree: DebugTree = DebugTreeController.decodeDebugTree(jsonTree).get
    
    "The tree" should "be deserialised" in {
        /* Check that the root tree has been deserialised correctly */
        tree.input should be ("Test")
        tree.root.nodeId should be (0)
        tree.root.name should be ("Test")
        tree.root.internal should be ("Test")
        tree.root.success should be (true)
        tree.root.childId should be (0)
        tree.root.input should be ("Test")
        tree.root.isLeaf should be (false)
        tree.root.doesNeedBubbling should be (false)
    }

    it should "not be deserialised if the JSON is not properly formatted" in {
        val wrongJson: String = "Testing..."
        DebugTreeController.decodeDebugTree(wrongJson) shouldBe a [Failure[_]]
    }
}