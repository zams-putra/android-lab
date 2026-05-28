package  com.sebassmith.darkmemories.model

import com.google.gson.annotations.SerializedName

data class Photo  (
    val id: Int,
    val title: String,
    val desc: String,
    @SerializedName("img_url") val imageURL: String,
    val date: String
)


data class  PhotoResponse(
    val photos: List<Photo>,
    val server: String,
    val path: String,
)