package view

import com.raquo.laminar.api.L.*

import model.InputViewNode

object InputViewNode {
    def apply(node: InputViewNode) = p(
        className := "input-view-node",
        node.source,
        // onClick -- > 
    )
}