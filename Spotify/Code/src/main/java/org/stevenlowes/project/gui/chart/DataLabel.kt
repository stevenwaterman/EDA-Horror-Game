package org.stevenlowes.project.gui.chart

import javafx.geometry.Pos
import javafx.scene.chart.NumberAxis
import javafx.scene.control.Label
import javafx.scene.text.Font
import javafx.scene.text.FontWeight
import kotlin.math.max
import kotlin.math.min

class DataLabel constructor(labelText: String,
                            val x: Number,
                            private val textAnchor: Pos = Pos.CENTER) {

    val node = Label()
    private var displayX: Double = x.toDouble()
    private var displayY: Double = 0.0

    var text: String
        get(){ return node.text }
    set(newText){
        node.text = newText
    }

    init {
        text = labelText
        this.node.widthProperty().addListener { _ -> layoutText() }
        this.node.heightProperty().addListener { _ -> layoutText() }
    }

    fun layout(fontSize: Int, xAxis: NumberAxis, yAxis: NumberAxis, yConverter: (Number) -> Number?) {
        val family = node.font.family
        node.font = Font.font(family, FontWeight.BOLD, fontSize.toDouble())

        displayX = xAxis.getDisplayPosition(x)
        var y = yConverter(x)?.toDouble()

        node.isVisible = y != null
        y ?: return

        val yRange = yAxis.upperBound - yAxis.lowerBound
        val yBuffer = yRange * 0.05
        y = min(y, yAxis.upperBound - yBuffer)
        y = max(y, yAxis.lowerBound + yBuffer)
        displayY = yAxis.getDisplayPosition(y)
        layoutText()
    }


    private fun layoutText() {
        // Initially the node width and height are 0 so we have to recompute the layout after the first rendering.  See the width and height property listeners.
        when (textAnchor) {
            Pos.TOP_CENTER, Pos.CENTER, Pos.BOTTOM_CENTER -> node.layoutX = displayX - node.width / 2
            Pos.TOP_LEFT, Pos.CENTER_LEFT, Pos.BOTTOM_LEFT -> node.layoutX = displayX
            Pos.TOP_RIGHT, Pos.CENTER_RIGHT, Pos.BOTTOM_RIGHT -> node.layoutX = displayX - node.width
            else -> throw IllegalStateException("${textAnchor.name} is not supported.")
        }
        when (textAnchor) {
            Pos.CENTER, Pos.CENTER_LEFT, Pos.CENTER_RIGHT -> node.layoutY = displayY - node.height / 2
            Pos.TOP_LEFT, Pos.TOP_CENTER, Pos.TOP_RIGHT -> node.layoutY = displayY
            Pos.BOTTOM_LEFT, Pos.BOTTOM_CENTER, Pos.BOTTOM_RIGHT -> node.layoutY = displayY - node.height
            else -> throw IllegalStateException("${textAnchor.name} is not supported.")
        }
    }
}
