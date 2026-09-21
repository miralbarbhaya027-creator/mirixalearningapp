package com.example.mirixa

data class Category(
    val id: String = "",
    val name: String = "",
    val courseCount: Int = 0,
    val iconType: String = ""
)

data class VideoLecture(
    val url: String = "",
    val title: String = "",
    val duration: String = ""
)

data class CourseTopic(
    val title: String = "",
    val description: String = "",
    val iconType: String = "code"
)

data class CourseNote(
    val pdfUrl: String = "",
    val topics: List<CourseTopic> = listOf()
)

data class MindmapNode(
    val id: String = "",
    val label: String = "",
    val x: Float = 0f,
    val y: Float = 0f
)

data class Mindmap(
    val image: String = "",
    val nodes: List<MindmapNode> = listOf()
)

data class Course(
    val id: String = "",
    val categoryId: String = "",
    val title: String = "",
    val description: String = "",
    val imageUrl: String = "",
    val videoLecture: VideoLecture? = null,
    val note: CourseNote? = null,
    val mindmap: Mindmap? = null,
    val quiz: List<Question>? = null
)
