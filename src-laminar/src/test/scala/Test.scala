import org.scalatest
import org.scalatest.flatspec.AnyFlatSpec   
import org.scalatest.matchers.*

import lib.DebugTree
import lib.DebugTreeHandler
import lib.DebugNode
import scala.util.{Try, Success, Failure}

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
            "children": [
                {
                    "nodeId": 1,
                    "name": "Test1",
                    "internal": "Test1",
                    "success": true,
                    "childId": 0,
                    "input": "Test1",
                    "children": [
                        {
                            "nodeId": 2,
                            "name": "Test2",
                            "internal": "Test2",
                            "success": true,
                            "childId": 0,
                            "input": "Test1.1",
                            "children": [],
                            "isLeaf": true
                        }
                    ],
                    "isLeaf": false
                },
                {
                    "nodeId": 3,
                    "name": "Test3",
                    "internal": "Test3",
                    "success": true,
                    "childId": 0,
                    "input": "Test2",
                    "children": [
                        {
                            "nodeId": 4,
                            "name": "Test4",
                            "internal": "Test4",
                            "success": true,
                            "childId": 0,
                            "input": "Test2.1",
                            "children": [],
                            "isLeaf": true
                        }
                    ],
                    "isLeaf": false
                }
            ],
            "isLeaf": false
        }
    }"""


    val tree: DebugTree = DebugTreeHandler.decodeDebugTree(jsonTree).get

    
    "The tree" should "be deserialised" in {
        /* Check that the root tree has been deserialised correctly */
        tree.input should be ("Test")
        tree.root.nodeId should be (0)
        tree.root.name should be ("Test")
        tree.root.internal should be ("Test")
        tree.root.success should be (true)
        tree.root.childId should be (0)
        tree.root.input should be ("Test")
        tree.root.children should have length 2
        tree.root.isLeaf should be (false)

        /* Check that the children have been deserialised correctly */
        var current_id = 0
        def test_node(current: DebugNode): Unit = {
            current_id += 1
            current.nodeId should be (current_id)
            current.name should be (s"Test${current_id}")
            current.internal should be (s"Test${current_id}")
            current.success should be (true)
            current.childId should be (0)
            current.children.foreach(test_node)
            current.isLeaf should be (current.children.isEmpty)
        }

    }

    it should "not be deserialised if the JSON is not properly formatted" in {
        val wrongJson: String = "Testing..."
        DebugTreeHandler.decodeDebugTree(wrongJson) shouldBe a [Failure[_]]
    }
}