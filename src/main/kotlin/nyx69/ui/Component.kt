package nyx69.ui

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.encodeToJsonElement
import nyx69.ui.ComponentType.*

@Serializable
data class Component(
    val id: String,
    val type: ComponentType,
    val data: @Contextual Any? = null,
    val children: List<Component>? = null
)

@Suppress("FunctionName")
object Widget {
    fun CText(id: String, text: String) = Component(id, TEXT,text)
    fun CEditText(id: String, text: String) = Component(id, TEXT, text)
    fun CImage(id: String, url: String) = Component(id, IMAGE, url)
    fun CButton(id: String, text: String) = Component(id, BUTTON, text)
}

@Suppress("FunctionName")
object Layout {
    fun CColumn(id: String, children: List<Component>) = Component(id, VERTICAL, children = children)
    fun CLazyColumn(id: String, children: List<Component>) =
        Component(id, SCROLL_VERTICAL, children = children)

    fun CBox(id: String, children: List<Component>) = Component(id, BOX, children = children)
}