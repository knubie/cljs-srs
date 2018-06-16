# Technical Debt
- [x] Figure out how to not coerce unserialized dates
   - [x] src/app/db
   - [x] to-local-date
   - [x] src/app/models/card
- [x] Break up workspaces file
- [~] Optimize queries
- [~] Add spec for notes
- [x] Put magic keyboard numbers in their own module.
- [x] Add spec for review
- [~] Run time spec checking
   - [x] Check db
   - [ ] Check ui
   - [] specs for events
- [] Come up with a system for scopes and queries
   - [] When do we return the collections with keys
   - [] When do we return the vals?
- [ ] Write specs for events
- [ ] Add docustrings to functions
- [x] Organize db file
- [~] Add note#show page
   - [ ] Add ability to edit note
   - [ ] Look into draft.js
- [ ] Generalize styles
   - [ ] Add support for custom colors (night mode)
- [~] Start writing tests
   - [x] Add tests for srs system
- [ ] Get resource/public/ shit into git
- [ ] Remove data transformations from views
- [ ] Make generic "editable" component

# Dogfood Req
- [] Backups
- [] Working SRS
- [] Furigana support
- [] Import from csv
   - [] Import from Anki
- [] Images on cards
- [] Audio on cards
- [] Backups

# Launch MVP
- [] Dogfood complete
- [] Add metadata to the deck table
   - [] Due
   - [] Maturity
- [x] Local storage sync
   - [x] Loads seed data if no localstorage is found.
   - [ ] Improve seed data ( Right now it's all japanese shit. )
- [] SRS System working
   - [x] Remember and forget cards
   - [] Learn new cards
- [] Fix UI quirks
   - [~] Hover / pointer on sidebar
   - [] Fix preview placement on deck table
   - [] Add something to the top of the side bar (or not)
- [ ] Figure out a better way to handle deck re-naming.
   - [ ] Right click context menu?
   - [ ] Edit title on show page?
- [x] Add Preview to card
   - [ ] Improve design of card preview
   - [ ] Add ability to edit template from preview
- [ ] Figure out search

# Later features
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
