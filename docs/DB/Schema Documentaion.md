# Database Schema Documentation

This document outlines the SQLite database schema used in the FrameSprite application.

## Table: `projects`
This table stores the metadata and canvas dimensions for user-created drawing projects.

| Column Name | Data Type | Constraints | Default Value | Description |
| :--- | :--- | :--- | :--- | :--- |
| `id` | `INTEGER` | `PRIMARY KEY`, `AUTOINCREMENT` | *None* | The unique identifier for the project. |
| `title` | `TEXT` | `NOT NULL` | *None* | The display name or title of the project. |
| `user_id` | `INTEGER` | `NOT NULL` | *None* | A reference to the user who owns/created the project. |
| `challenge_id` | `INTEGER` | `NOT NULL` | *None* | A reference to the challenge this project is associated with. |
| `height` | `INTEGER` | *None* | `600` | The height of the project's canvas in pixels. |
| `width` | `INTEGER` | *None* | `800` | The width of the project's canvas in pixels. |
| `last_modified` | `DATETIME` | *None* | `CURRENT_TIMESTAMP` | The timestamp indicating when the project was created or last updated. |

---

## Relationships

* **`user_id`** → Belongs to `users.id`
    * **Relationship:** 1-to-Many. (A User can have many Projects).
* **`challenge_id`** → Belongs to `challenges.id`
    * **Relationship:** 1-to-Many. (A Challenge can have many Projects associated with it).
