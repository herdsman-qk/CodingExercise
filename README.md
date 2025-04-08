# Android Coding Exercise - Fetch and Display List of Items

## How the App Works
1. Fetch Data: The app uses **Retrofit** to fetch a list of items from a remote JSON file.

2. Process Data: The app filters out items with empty or null names, then groups them by listId and sorts them by name.

3. Display Data: The app uses **ExpandableListView** to show the grouped and sorted list of items.
