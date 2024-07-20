PoEssence is a tool that combines information from websites poe.ninja and pathofexile.com/trade (Bulk Item Exchange).

General features:

Shortcuts:
Alt+Z/C or Alt+Left/Right Arrow - switches between adjacent snapshots on current tab.
If used during refresh - new data will be lost unless it will be autosaved on success.
In this case better to Stop it and restart.

What data is saved:
Settings are saved on exit.
Notes are saved on "Save" button in them.
Snapshots of loaded data are saved:
- Manually by pressing "Save" button.
- Automatically when "Refresh prices (tab)" fifnishes without constant update. (See details in Buttons overview)
- In other cases only table is updated, but nothing is saved.

Option to select wanted items appears on first execution on active tab or on every button "Refresh prices (tab) + Filter" click.

Settings overview:
Account name to highlight your offers.
Default empty.
Minimal essence tier filter.
Default is 6.
Highlight of minimal margin (Diff %) between divine value of ninja price vs. lowest divine value of poe bulk trade.
Default is 75%.
Multiplier of poe.ninja price that is reflected on Diff % live in order to estimate if e.g. bulk buy from TFT discord makes sence.
Default is 100%.
League dropdown - self-explanatory.
NOTE: snapshots does not care for what league do you save them, be careful.
Snapshot dropdown - content changes depending on active tab, shows previouslt successfully saved data.
Save / Load - save/load selected snapshot.
Invalidate saves - moves saved data of current item type to /old folder.
It becomes inaccessible for loading and graphs and can be restored manually.

Graphs: creates basic graphs of individual and bulk prices per item type, based on all saved data.

Buttons overview:
(Use "Constant update" with caution - may hang on background on exit or lead to timeouts on poe trade site)
Refresh prices (tab):
1) Requests data from poe.ninja for active tab.
2) Shows filter window (on first use).
3) Starting data requests to poe trade. Due to specific of website, delay between requests is needed.
   Tries to show progress and estimate on the buttom of window.
   4.1) (Optional) If "Constant update" checkbox was checked before start
    - continually repeats requests to poe trade, using extended delay after first fill.
      4.2)  (Optional) If "Constant update" checkbox was not checked before start
    - saves data as snapshot.

Refresh prices (tab) + Filter:
Same as "Refresh prices (tab)" but shows Filter window every time.

Stop:
(Tries to) Stop all upcoming pathofexile.com/trade requests.
NOTE: It is possible that after closing of this tool background task that was started with "Constant update" will continue to run.
If you have some doubts about that:
Open Task Manager and look for Java process that consumes considerable amount of CPU/Disk -> End it.
(Unless you know that some other soft on your PC may run java processes (why tho, r u crazy?)).

Refresh all (no autosave):
Same as "Refresh prices (tab)", but it never saves results, whenever "Constant update" is checked or not.

Table overview:

"Note" - opens note, that is stored per item type. Will be saved on "Save" button, will persist between refreshes and tool restarts.
"Div Value" of items are calculated based ob chaos value of item from poe.ninla / divine value in chaoses from poe.ninja every time.
"Bulk Div" is lowest price per item from bulk trade.
"Diff %" see Settings overview.
"Account" - name of seller.
"Div Зrice" - how many divines per item. In case if more then 1 divine is asked, value is normalized, original value is in ().
"Stock" - how many items available.

Bulk offers are colored:
- Green if player is online.
- Yellow if player is AFK.
- Blue if player name is equal to Account name from field under the table.


To develop it I use OpenJDK with OpenJFX included.