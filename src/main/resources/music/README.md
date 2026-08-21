# Mizuki Music playlist

Add `.wav` or `.mp3` files to `music/tracks/`, then register each track in
`playlist.json`:

```json
{
  "id": "unique-track-id",
  "title": "Track title",
  "artist": "Artist name",
  "resource": "/music/tracks/file-name.mp3"
}
```

Only `id`, `title`, and `resource` are required. `artist` may be left empty.
