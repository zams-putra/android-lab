package handler

import (
	"encoding/json"
	"net/http"

	"github.com/zams-putra/android-lab/DarkMemories/server/constants"
	"github.com/zams-putra/android-lab/DarkMemories/server/internal/model"
)

var photosData = []model.Photo{
	{
		ID:       1,
		Title:    "MySelf",
		Desc:     "Foto diri ku sendiri, di ambil dari jarak beberapa meter",
		ImageURL: "http://10.0.2.2:8080/img/1.jpg",
		Date:     "2026-05-26",
	},
	{
		ID:       2,
		Title:    "Relaxing View",
		Desc:     "Foto ombak di malam gelap dan mendung, sungguh mimpi indah",
		ImageURL: "http://10.0.2.2:8080/img/2.jpg",
		Date:     "2026-02-21",
	},
	{
		ID:       3,
		Title:    "Daily",
		Desc:     "Foto kegiatan yang sering dilakukan sehari hari",
		ImageURL: "http://10.0.2.2:8080/img/3.jpg",
		Date:     "2026-02-22",
	},
	{
		ID:       4,
		Title:    "Lonewolf",
		Desc:     "Foto diriku di universe lain",
		ImageURL: "http://10.0.2.2:8080/img/4.jpg",
		Date:     "2025-02-22",
	},
	{
		ID:       5,
		Title:    "Liminal",
		Desc:     "Genre view dan vibes favoritku",
		ImageURL: "http://10.0.2.2:8080/img/5.jpg",
		Date:     "2020-04-22",
	},
}

func ServerIMG(w http.ResponseWriter, r *http.Request) {
	http.StripPrefix("/img/", http.FileServer(http.Dir("./img"))).ServeHTTP(w, r)
}

func GetPhotos(w http.ResponseWriter, r *http.Request) {
	apiKey := r.Header.Get("X-API-KEY")
	if apiKey != constants.API_KEY {
		http.Error(w, "Unauthorized", 401)
		return
	}

	w.Header().Set("Content-Type", "application/json")
	json.NewEncoder(w).Encode(map[string]any{
		"photos": photosData,
		"server": "internal-photo-server-v1",
		"path":   "/var/www/images",
		"msg":    "We have secret directory right :v ",
	})
}
