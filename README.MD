# PoEssence

**PoEssence** is a powerful tool that combines information from [poe.ninja](https://poe.ninja) and [pathofexile.com/trade](https://pathofexile.com/trade) to facilitate bulk item exchanges in Path of Exile.

## Installation

1. Go to the [latest release](https://github.com/Dscorp42/PoEssence/releases/latest).
2. Download and run `PoEssence.exe`.

> **Note:** Only the installed version supports auto-updates.

## Prerequisites

To run PoEssence without installation, you need to have Java JDK installed. You can download it from [Oracle](https://download.oracle.com/java/22/archive/jdk-22.0.1_windows-x64_bin.exe).

## General Features

### Shortcuts

- **Alt+Z/C** or **Alt+Left/Right Arrow**: Switches between adjacent snapshots on the current tab.
  - **Note:** If used during a refresh, new data may be lost unless it is autosaved on success. It is better to stop the refresh and restart it if needed.

### Data Management

- **Settings**: Saved upon exit.
- **Notes**: Saved using the "Save" button in the notes section.
- **Snapshots**:
  - Manually saved by pressing the "Save" button.
  - Automatically saved when "Refresh prices (tab)" finishes without a constant update.
  - In other cases, only the table is updated; no data is saved.

**Note:** The option to select wanted items appears on first execution on the active tab or every time you click "Refresh prices (tab) + Filter."

## Settings Overview

- **Account Name**: Used to highlight your offers. Default is empty.
- **Minimal Essence Tier Filter**: Default is 6.
- **Highlight of Minimal Margin**: The minimum margin (Diff %) between the divine value of ninja price vs. the lowest divine value of Poe bulk trade. Default is 75%.
- **Multiplier of Poe.ninja Price**: Reflects the Diff % live to estimate if bulk buying from TFT discord makes sense. Default is 100%.
- **League Dropdown**: Self-explanatory. **Note:** Snapshots do not consider the league in which you save them, so be careful.
- **Snapshot Dropdown**: Changes depending on the active tab and shows previously successfully saved data.
- **Save / Load**: Save/load the selected snapshot.
- **Invalidate Saves**: Moves saved data of the current item type to the `/old` folder, making it inaccessible for loading and graphs. It can be restored manually.

## Graphs

- Creates basic graphs of individual and bulk prices per item type based on all saved data.

## Buttons Overview

- **Refresh Prices (Tab)**:
  1. Requests data from poe.ninja for the active tab.
  2. Shows the filter window (on first use).
  3. Starts data requests to Poe trade. Due to the nature of the website, a delay between requests is needed and progress is estimated at the bottom of the window.
  4. (Optional) If "Constant update" is checked before start:
       - Continually repeats requests to Poe trade, using an extended delay after the initial fill.
  5. (Optional) If "Constant update" is not checked:
       - Saves data as a snapshot.

- **Refresh Prices (Tab) + Filter**:
  - Same as "Refresh Prices (Tab)" but shows the filter window every time.

- **Stop**:
  - Attempts to stop all upcoming pathofexile.com/trade requests.
  - **Note:** A background task started with "Constant update" may continue running after closing the tool. If in doubt, open Task Manager and look for the Java process consuming considerable CPU/Disk resources and end it.

- **Refresh All (No Autosave)**:
  - Same as "Refresh Prices (Tab)", but never saves results, regardless of whether "Constant update" is checked.

## Table Overview

- **Note**: Opens a note stored per item type. Will be saved on the "Save" button and will persist between refreshes and tool restarts.
- **Div Value**: Calculated based on chaos value of the item from poe.ninja / divine value in chaoses from poe.ninja every time.
- **Bulk Div**: Lowest price per item from bulk trade.
- **Diff %**: See Settings Overview.
- **Account**: Name of the seller.
- **Div Price**: Number of divines per item. If more than 1 divine is asked, the value is normalized, and the original value is in parentheses.
- **Stock**: Number of items available.

**Bulk offers are colored:**
- **Green**: If the player is online.
- **Yellow**: If the player is AFK.
- **Blue**: If the player name matches the Account name from the field under the table.

## Development

To develop PoEssence, I use OpenJDK with OpenJFX included.

---

For any issues or contributions, please feel free to open an issue or submit a pull request on the [GitHub repository](https://github.com/Dscorp42/PoEssence).
