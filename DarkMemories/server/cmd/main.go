package main

import (
	"log"
	"net/http"

	"github.com/zams-putra/android-lab/DarkMemories/server/internal/handler"
)

func main() {

	mux := http.NewServeMux()

	mux.HandleFunc("/photos", handler.GetPhotos)
	mux.HandleFunc("/s3cr3t_n4sg0r_g0r3ng", handler.GetSecret)
	mux.HandleFunc("/img/", handler.ServerIMG)

	log.Println("run on http://127.0.0.1:8080")
	http.ListenAndServe(":8080", mux)
}
