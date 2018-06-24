# Technical Debt
- [] Change add-record! signature to make record first arg, update import.cljs
- [ ] Performance monitoring
   - When are components re-rendering
   - When are they querying the database when they don't need to?
      - See: side-bar/deck-item.
      - It rerenders when the background changes, meaning it re-queries the
        db for its decks every time. We could cache the result using r/track
        or something like that.
- [ ] Break out data-table into separate files
   - [ ] rename to deck-table
- [ ] Strip newlines from table columns, etc.
- [ ] Fix the isOver behavior for dragging decks around
- [ ] Use keywords instead of strings for field type
   - [ ] update src/app/views/data-table
- [~] Optimize queries
- [~] Add spec for notes
- [~] Run time spec checking
   - [x] Check db
   - [ ] Check ui
   - [ ] specs for events
- [ ] Come up with a system for scopes and queries
   - [ ] When do we return the collections with keys
   - [ ] When do we return the vals?
- [ ] Write specs for events
- [ ] Add docustrings to functions
- [~] Add note#show page
   - [ ] Add ability to edit note
   - [ ] Look into draft.js
- [ ] Generalize styles
   - [ ] Add support for custom colors (night mode)
- [~] Start writing tests
   - [x] Add tests for srs system
- [ ] Remove data transformations from views
- [ ] Make generic "editable" component

# Dogfood Req
- [ ] Backups
- [ ] Working SRS
- [ ] Furigana support
- [ ] Import from csv
   - [ ] Import from Anki
- [x] Images on cards
- [x] Audio on cards
- [ ] Backups

# Launch MVP
- [ ] Link notes to cards
   - [] Add a "notes" field type
      - Contains a list of note IDs
   - [] When the card is rendered, it will have links for each linked note
   - [] When clicked, there will be a popup with the note in it.
- [ ] Saved filters, acts like Anki, UI like notion.
   - Notion UI like table views?
- [ ] Write a blog post on how to use Memo for language learning.
- [~] Add a default "Welcome to Memo" note
- [ ] Dogfood complete
- [x] Add metadata to the deck table
   - [x] Due
   - [~] Maturity
- [x] Local storage sync
   - [x] Loads seed data if no localstorage is found.
   - [ ] Improve seed data ( Right now it's all japanese shit. )
- [x] SRS System working
   - [x] Remember and forget cards
   - [x] Learn new cards
- [ ] Fix UI quirks
   - [~] Hover / pointer on sidebar
   - [ ] Fix preview-button placement on deck table
   - [ ] Add something to the top of the side bar (or not)
- [ ] Figure out a better way to handle deck re-naming.
   - [ ] Right click context menu?
   - [ ] Edit title on show page?
- [x] Add Preview to card
   - [ ] Improve design of card preview
   - [ ] Add ability to edit template from preview
- [ ] Figure out search

# Later features
- [ ] Two card templates.
   - [ ] One for the SRS Card
   - [ ] One for embedding the card into notes
- [ ] Add cards view to decks#show
- [ ] Refactor state/ui to allow for undoing/going back
   - [ ] Separate events into db events and ui events
   - [ ] Add support for browser history
- [ ] Settings page
- [ ] Transfer one card's review history to another card.
- [ ] Add wanikani style maturity labels
- [ ] Editting a row (card) in full screen, brings up the template + data and is editable. The fields are separated by dashes. Do we even need explicit fields at this point? Just use the dashes..

# Features
- Turn note into a deck of cards, by looking for bulleted list.
- When a card lapses, reduce the ease by 80%. If 80% isn't small enough to make any change in the next interval, reduce it more.
- Separate "study" and "learn." Learn will cycle through five cards at a time until you've "learned" them.
- Think about how extra hidden fields are used in your RTK deck. Some fields have vocab info, some might have etymology, etc. These act as notes, in a way.
- Some cards are "linked" to others. Maybe dependenies?
   - E.g. This vocab is "blocked" by this kanji. Once this kanji is learned, unblock the vocab. Or don't show the vocab before the kanji in review.
