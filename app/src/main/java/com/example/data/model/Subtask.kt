package com.example.data.model

import java.util.UUID

data class Subtask(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val isCompleted: Boolean = false
)

object SubtaskHelper {
    fun fromJson(json: String?): List<Subtask> {
        if (json.isNullOrBlank() || json == "[]") return emptyList()
        return try {
            val arr = org.json.JSONArray(json)
            val list = mutableListOf<Subtask>()
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                list.add(
                    Subtask(
                        id = obj.optString("id", UUID.randomUUID().toString()),
                        title = obj.optString("title", ""),
                        isCompleted = obj.optBoolean("isCompleted", false)
                    )
                )
            }
            list
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun toJson(list: List<Subtask>): String {
        val arr = org.json.JSONArray()
        for (item in list) {
            val obj = org.json.JSONObject()
            obj.put("id", item.id)
            obj.put("title", item.title)
            obj.put("isCompleted", item.isCompleted)
            arr.put(obj)
        }
        return arr.toString()
    }
}
