import org.scalatest
import org.scalatest.flatspec.AnyFlatSpec   
import org.scalatest.matchers.*

import debugger.DebugTree
import debugger.DebugTreeHandler

@Test
class Test extends AnyFlatSpec with should.Matchers {
    val jsonTree: String = """input": "Test",
            "root": {
                "name": "Test",
                "internal": "Test",
                "success": true,
                "number": 0,
                "input": "Test",
                "children": [
                    {
                        "name": "Test1",
                        "internal": "Test1",
                        "success": true,
                        "number": 0,
                        "input": "Test1",
                        "children": [
                            {
                                "name": "Test1.1",
                                "internal": "Test1.1",
                                "success": true,
                                "number": 0,
                                "input": "Test1.1",
                                "children": []
                            }
                        ]
                    },
                    {
                        "name": "Test2",
                        "internal": "Test2",
                        "success": true,
                        "number": 0,
                        "input": "Test2",
                        "children": [
                            {
                                "name": "Test2.1",
                                "internal": "Test2.1",
                                "success": true,
                                "number": 0,
                                "input": "Test2.1",
                                "children": []
                            }
                        ]
                    }
                ]
            }
        }"""

    val handler: DebugTreeHandler = DebugTreeHandler()
    val tree: DebugTree = handler.decodeDebugTree(jsonTree).get
    
    "The tree" should "be serialised" in {
        /* Check that the root tree has been serialised correctly */
        tree.input should be ("Test")
        tree.root.name should be ("Test")
        tree.root.internal should be ("Test")
        tree.root.success should be (true)
        tree.root.number should be (0)
        tree.root.input should be ("Test")
        tree.root.children.len() should be (2)

        /* Check that the children have been serialised correctly */
        for ((index, child) <- tree.root.children) {
            child.name should be (("Test" + Int.toString(index + 1)))
            child.internal should be (("Test" + Int.toString(index + 1)))
            child.success should be (true)
            child.number should be (0)
        }

    }

    it should "not be deserialised if the JSON is not properly formatted" in {
        val wrongJson: String = "Testing..."
        handler.decodeDebugTree(jsonTree) should be a 'failure
    }
}