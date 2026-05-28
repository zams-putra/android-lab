package handler

import (
	"encoding/json"
	"net/http"

	"github.com/zams-putra/android-lab/DarkMemories/server/constants"
)

func GetSecret(w http.ResponseWriter, r *http.Request) {
	apiKey := r.Header.Get("X-API-KEY")
	if apiKey != constants.API_KEY {
		http.Error(w, "Unauthorized", 401)
		return
	}

	w.Header().Set("Content-Type", "application/json")
	json.NewEncoder(w).Encode(map[string]any{
		"flag": "FLAG{796f65bbc80d99a89e2b5b54e18d2e48}",
		"msg":  "Congrats yh",
	})
}
