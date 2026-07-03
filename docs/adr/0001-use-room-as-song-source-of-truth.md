# Use Room as the song source of truth

The app should render song list and song detail content from Room, while remote API calls run as refresh operations that update Room. This keeps previously synced content visible during slow, failed, or cold-starting backend requests; only an empty cache makes refresh failure block content.
