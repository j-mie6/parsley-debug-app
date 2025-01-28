import org.scalatest
import org.scalatest.flatspec.AnyFlatSpec   
import org.scalatest.matchers.*

import lib.DebugTree
import lib.DebugTreeHandler
import scala.util.{Try, Success, Failure}

class Test extends AnyFlatSpec with should.Matchers {

    val jsonTree: String = """{
        "input": "Test",
        "root": {
            "name": "Test",
            "internal": "Test",
            "success": true,
            "childId": 0,
            "input": "Test",
            "children": [
                {
                    "name": "Test1",
                    "internal": "Test1",
                    "success": true,
                    "childId": 0,
                    "input": "Test1",
                    "children": [
                        {
                            "name": "Test1.1",
                            "internal": "Test1.1",
                            "success": true,
                            "childId": 0,
                            "input": "Test1.1",
                            "children": []
                        }
                    ]
                },
                {
                    "name": "Test2",
                    "internal": "Test2",
                    "success": true,
                    "childId": 0,
                    "input": "Test2",
                    "children": [
                        {
                            "name": "Test2.1",
                            "internal": "Test2.1",
                            "success": true,
                            "childId": 0,
                            "input": "Test2.1",
                            "children": []
                        }
                    ]
                }
            ]
        }
    }"""


    val tree: DebugTree = DebugTreeHandler.decodeDebugTree(jsonTree).get

    
    "The tree" should "be deserialised" in {
        /* Check that the root tree has been deserialised correctly */
        tree.input should be ("Test")
        tree.root.name should be ("Test")
        tree.root.internal should be ("Test")
        tree.root.success should be (true)
        tree.root.childId should be (0)
        tree.root.input should be ("Test")
        tree.root.children should have length 2

        /* Check that the children have been deserialised correctly */
        for ((child, index) <- tree.root.children.zipWithIndex) {
            child.name should be (s"Test${index + 1}")
            child.internal should be (s"Test${index + 1}")
            child.success should be (true)
            child.childId should be (0)
        }

    }

    it should "not be deserialised if the JSON is not properly formatted" in {
        val wrongJson: String = "Testing..."
        DebugTreeHandler.decodeDebugTree(wrongJson) shouldBe a [Failure[_]]
    }
}