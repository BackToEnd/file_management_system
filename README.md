## Road Map
Here is a full list of all features I have planned for this application. Some of them are pretty broad and basic, others a bit more edge-case-specific. All completed features will be moved to the top of this list. If you have any other suggestions or ideas, feel free to reach out; I'll try to implement as many suggestions as I can.
#### Core Features:
- [x] Opening and parsing directory paths from your device, and writing the retrieved information into a database for storage
- [ ] Displaying all stored information in a simplistic overview that is easy to navigate and understand
- [ ] Adding custom metadata such as tags to any discovered files, and filtering displayed items by metadata
- [ ] Automatically rescanning directories at app launch to detect changes in folder structure, and notify the user that files or directories have been moved or deleted
- [ ] Recursively scan not only the direct content of a selected directory, but also browse the content of its sub-folders
- [ ] Manually disable scanned paths you no longer want to work with from the database, and fully delete them
- [ ] Allow parent directories to define what metadata children can have, and define default values for children metadata
- [ ] Advanced filter, search, and sort options, possibly also including a "query builder"
- [ ] Collections: save filter combinations as filter presets (Collections), to be able to instantly show only items matching all conditions without having to manually select all conditions first. Define them once, reuse them indefinitely!
- [ ] Proper metadata templates: let the parent directory define what metadata types every child item should automatically have, and save time manually selecting everything while editing the child item
- [ ] Duplicate file detection: the application can guess when a newly found directory or file is potentially a duplicate of an existing item, saving you the trouble from manually dealing with renamed items
- [ ] Various different metadata types: yeah, a regular String word probably suffices, but how about dates, booleans, integers, float-point numbers, or even linking together otherwise completely unrelated files?
- [ ] Storage analytics tools: display statistics about scanned directories, such as folder size, file counts, file extensions/files... possibly also with a nice visual display
- [ ] Bulk metadata application: select multiple scanned items, define what metadata to apply to all, and let the application do everything in the background while you can do more useful things, instead of having to add metadata to 40 items by hand, one by one
- [ ] More complex metadata assignment rule systems: assign specific metadata to items based on file type, size, last modified date, what drive they are stored in, etc.
- [ ] Performing actual file operations: copying, moving, renaming, and deleting actual files on your system - the core features of this app would still work without touching your actual files though
- [ ] Inspecting the content of ZIP archives
- [ ] Previewing supported files - such as PDFs and various image formats - directly inside this application
- [ ] Opening file explorers to any scanned item from inside the application
- [ ] Work spaces: similar to Collections, but via dedicated workspace metadata rather than tags and other filters - group together any files that belong to one specific work space; organize work projects and personal files within the same app, but across different workspaces
- [ ] Persistent settings: currently, settings such as language selection are session-based and will disappear once you close the app. This feature introduces a settings file that allows you to customize things like default language once, and have them all apply each time you open the app.
- [ ] Importing and exporting organization schemes such as metadata templates, categories, saved queries, workspaces, etc.
- [ ] Digital art library: organize digital art projects with image previews
- [ ] Git integration: introduces version control features through Git; for example, automated fetching, committing, pushing of project files, being notified if changes were made to origin files, viewing which scanned directories and files belong to what repo, branch, or worktree
- [ ] Natural language search: LLM integration as an addition to the visual query builder, allowing you to search for keywords instead of configuring the query through filter options and drop-downs. This feature would be entirely optional, and none of your personal files or any scanned information would ever leave your device - only a list of metadata types you've defined, and the prompt you type
- [ ] Proper graph visualization of scanned directories: replace the tree table view display with something more aesthetically pleasing, such as an Obsidian-style graph view, or a proper hierarchy tree structure with zoom and panning support
