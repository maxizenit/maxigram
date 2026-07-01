-- De-anonymization now converts the anonymous chat in place (anonymous -> false) instead of
-- creating and linking a separate regular chat: in a 1-on-1 chat, revealing identities makes
-- message authorship derivable anyway, and one continuous thread is a better experience.
alter table chat
    drop column new_chat_id;
