import scala.util.{Try, Success, Failure}
import org.scalatest
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.*

import model.DebugTree
import model.DebugNode
import model.json.{Reader, Writer}
import model.errors.DillException
import com.raquo.airstream.state.Var


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
            "isIterative": false,
            "newlyGenerated": false
        },
        "parserInfo": {},
        "isDebuggable": false,
        "refs": [],
        "sessionId": -1,
        "sessionName": "tree"
    }"""


    "The tree" should "be deserialised" in {
        val parsed: Either[DillException, DebugTree] = Reader[DebugTree].read(jsonTree)
        val tree: DebugTree = parsed match {
            case Left(e) => throw new Exception(s"Failed to serialise tree, got DillException: $e")
            case Right(tree) => tree
        }

        /* Check that the root tree has been deserialised correctly */
        tree.input should be ("Test")
        tree.sessionId should be (-1)
        tree.isDebuggable should be (false)
        tree.refs should be (Nil)

        tree.root.nodeId should be (0)
        tree.root.name should be ("Test")
        tree.root.internal should be ("Test")
        tree.root.success should be (true)
        tree.root.childId should be (0)
        tree.root.input should be ("Test")
        tree.root.isLeaf should be (false)
        tree.root.isIterative should be (false)
        tree.root.newlyGenerated should be (false)
    }

    it should "not be deserialised if the JSON is not properly formatted" in {
        val wrongJson: String = "Testing..."
        assert(Reader[DebugTree].read(wrongJson).isLeft)
    }
}

import view.{FileObject, RootDirectory, Directory, File}

class FileSystemTest extends AnyFlatSpec with should.Matchers {
    "List of file paths" should "be converted" in {
        val paths: List[String] = List(
            "/dir/content/scala/test.scala",
            "/dir/content/scala/test2.scala",
            "/dir/content/rust/test.rs",
            "/dir/content/test.scala"
        )

        FileObject.fromPaths(paths) should matchPattern {
            case RootDirectory(List(
                Directory(
                    "content/",
                    List(
                        Directory(
                            "scala/",
                            List(
                                File("test.scala", "/dir/content/scala/test.scala"),
                                File("test2.scala", "/dir/content/scala/test2.scala"),
                            ),
                            _: Var[Boolean]
                        ),
                        File("rust/test.rs", "/dir/content/rust/test.rs"),
                        File("test.scala", "/dir/content/test.scala"),
                    ),
                    _: Var[Boolean]
                )
            )) =>
        }
    }

    "Single file path" should "be converted" in {
        val paths = List("/dir/scala/main/scala3/file.scala")

        FileObject.fromPaths(paths) shouldEqual RootDirectory(List(
            File("file.scala", "/dir/scala/main/scala3/file.scala")
        ))
    }
}
