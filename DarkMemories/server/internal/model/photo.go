package model

type Photo struct {
	ID       int    `json:"id"`
	Title    string `json:"title"`
	Desc     string `json:"desc"`
	ImageURL string `json:"image_url"`
	Date     string `json:"date"`
}
