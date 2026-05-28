package model

type Photo struct {
	ID       int    `json:"id"`
	Title    string `json:"title"`
	Desc     string `json:"desc"`
	ImageURL string `json:"img_url"`
	Date     string `json:"date"`
}
