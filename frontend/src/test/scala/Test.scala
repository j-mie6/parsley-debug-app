import scala.util.{Try, Success, Failure}
import org.scalatest
import org.scalatest.flatspec.AnyFlatSpec   
import org.scalatest.matchers.*

import upickle.default as up

import model.DebugTree
import model.DebugNode


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
            "isIterative": false
        }
    }"""

    val tree: DebugTree = up.read[DebugTree](jsonTree)
    
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
        tree.root.isIterative should be (false)
    }

    it should "not be deserialised if the JSON is not properly formatted" in {
        val wrongJson: String = "Testing..."
        Try(up.read[DebugTree](wrongJson)) shouldBe a [Failure[?]]
    }
}