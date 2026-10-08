# Changelog

## 1.0.0

### Breaking changes
- minSdk is now 21.
- Library resources are prefixed with `msv_`. Apps that overrode `EditTextStyle`, `view_item` or the
  other library resources by name need to use the new names.
- `MultiSearchContainerView` and the helper classes are internal.
- The listener receives `String` snapshots instead of the live `Editable`.

### Fixes
- `onTextChanged` and `onSearchComplete` report the index of the edited tab.
- `searchTextStyle` no longer resets the paddings and the typeface, and reads `textColorLink`
  from the right attribute.
- Tapping a tab after another one was removed no longer reports it as selected again.
- The indicator moves to the right tab after a removal.
- Tapping another tab while typing completes the current search.
- The completed tab keeps collapsing when another tab is selected during the animation.
- The keyboard is shown and hidden through the search field, without changing the soft input mode
  of the host window.
- Animations are cancelled when the view is detached.
- `search()` before the first layout waits for the view to be measured.
- The tab being typed follows width changes.
- The Enter key of hardware keyboards completes the search.
- Queries made only of whitespace are discarded.
- `setEnabled(false)` disables the view.

### Performance
- ViewBinding replaces DataBinding, so kapt is no longer needed.
- Two redundant `RelativeLayout` levels were removed.
- The enter animation no longer stacks `smoothScrollTo` calls.
- The global layout listener used to wait for a new tab is replaced by `doOnLayout`.
- Tab bindings are reused instead of being bound again.

### New
- `search()`, `completeSearch()` and `isInSearchMode`.
- `addSearch()`, `removeSearch()`, `clearSearches()`, `selectSearch()`, `queries` and
  `selectedIndex`.
- Searches are kept across configuration changes.
- `onSearchModeChanged` listener method. All listener methods have default implementations.
- `app:minSearchLength`, `app:animationDuration`, `app:searchIcon`, `app:removeIcon`,
  `app:iconTint` and `app:indicatorColor` attributes.
- Dark theme, right-to-left layout and TalkBack support, with a Turkish translation.

## 0.3
- Migrated to AndroidX.

## 0.2
- `app:searchTextStyle` attribute.
