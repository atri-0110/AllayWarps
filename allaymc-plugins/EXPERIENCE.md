# AllayMC Plugin Review Experience

## ItemMail (Reviewed 2026-02-03)

### What Went Well
- Comprehensive command system with multiple send options (hand, slot, all)
- Good separation of concerns (manager, commands, utils)
- Proper NBT serialization for item state preservation
- Thread-safe synchronized methods in MailManager
- Automatic cleanup of expired mail (30 days)
- Nice UX with item drops when inventory is full
- Good use of Lombok for data classes

### Critical Issue Fixed
**NbtMap Serialization Failure**: Gson cannot serialize List<NbtMap> without custom adapter
- Created `NbtMapAdapter` class to handle NbtMap <-> JSON conversion
- This was a **critical bug** that would prevent saving/loading mail items
- Same pattern as DeathChest - NbtMap requires custom Gson adapter
- Registered in Gson builder in MailManager

### Other Issues Fixed
1. **Memory Leak in Notifications**: `notifiedPlayers` Set grew indefinitely
   - Old: Players added to Set and never removed
   - New: Changed to Map<UUID, Long> with timestamp-based cooldown
   - Added 5-minute notification cooldown to prevent spam
   - Players re-notified after cooldown expires if they still have mail

2. **Code Duplication**: Player name retrieval repeated 7 times
   - Added `getPlayerName(EntityPlayer)` helper method
   - Reduced repetitive ternary operator pattern
   - Cleaner, more maintainable code

3. **Unused Utility Class**: Removed NotificationUtils
   - Class defined but never imported or used
   - All notifications done inline in commands
   - Cleaner codebase without dead code

### Observations for Future Plugins
- Mail limit (54) matches a double chest - good design choice
- ItemUtils has good separation for item operations
- Consider making expiration time configurable (currently 30 days)
- Claim command drops items on ground when inventory full - good UX
- `/mail claimall` is a nice convenience feature

### Code Quality Patterns to Follow
- Synchronized methods in MailManager for thread safety
- Clear command tree structure with intuitive subcommands
- Proper scheduler usage for periodic tasks
- MailStorage inner class for clean JSON structure
- Good error messages that guide users

### API Notes
- Same NBT serialization pattern as DeathChest
- `Player` vs `EntityPlayer` distinction in forEachPlayer callbacks
- `player.tryAddItem()` returns boolean and modifies count if needed
- `player.getDimension().dropItem()` for dropping items in world

---

## DeathChest (Reviewed 2026-02-03)

### What Went Well
- Excellent code structure with clear separation of concerns
- Proper use of NBT serialization to preserve item state
- Good command tree structure with helpful subcommands
- Thread-safe implementation using ConcurrentHashMap
- Automatic cleanup of expired chests with scheduler
- Comprehensive error handling and logging
- Good user feedback messages

### Critical Issue Fixed
**NbtMap Serialization Failure**: Gson cannot serialize NbtMap objects without custom adapter
- Created `NbtMapAdapter` class to handle NbtMap <-> JSON conversion
- This was a **critical bug** that would cause data loss or corrupted save files
- Adapter handles nested NbtMaps, arrays, and primitive types properly
- Registered in adapter in Gson builder configuration

### Other Issues Fixed
1. **Partial Recovery Data Loss**: Fixed chest recovery logic
   - Old: Marked chest as recovered only on 100% success, preventing retry
   - New: Always marks as recovered to prevent duplicate recovery loops
   - Added better feedback for partial failures

2. **Null World Reference**: Added safety check in DeathListener
   - Added null check for `dimension.getWorld()` to prevent NPE
   - Logs error if player dies in dimension without world

3. **Locale-Dependent Date Format**: Fixed date serialization
   - Added `Locale.US` to SimpleDateFormat to ensure consistent format
   - Prevents display issues on non-English systems

4. **Missing Item Count**: Added item count display in chest list
   - Shows number of items in each death chest
   - Helps players know what they'll recover

### Observations for Future Plugins
- When using Gson with NBT/complex objects, create custom TypeAdapter
- AllayMC ItemStack uses NBT for complete state preservation
- Consider making expiration time configurable (currently hardcoded 24h)
- The plugin stores items in JSON files - consider database for large scale
- Good pattern: load player data on demand, save on modification

### Code Quality Patterns to Follow
- Proper scheduler usage for periodic cleanup (every 30 min)
- Clean manager pattern for data operations
- Good use of Lombok @Data for POJOs
- Static getInstance() for plugin access
- Proper resource cleanup in onDisable

### API Notes
- AllayMC's `ItemStack.saveNBT()` returns `NbtMap`
- Use `NBTIO.getAPI().fromItemStackNBT()` for deserialization
- Container access: `player.getContainer(ContainerTypes.INVENTORY)`
- Dimensions have `getWorld()` method (may be null in edge cases)

---

## SimpleTPA (Reviewed 2026-02-03)

### What Went Well
- Clean code structure with proper separation of concerns
- Good use of ConcurrentHashMap for thread safety
- Proper API usage and command registration
- Clear and informative user messages
- Good error handling throughout

### Issues Found and Fixed
1. **Grammatical Error**: Fixed message in TpaHereCommand.java
   - Changed: "You cannot teleport yourself to yourself!"
   - To: "You cannot ask yourself to teleport to you!"

2. **Missing Cooldown Protection**: Added 10-second cooldown between requests
   - Prevents spam teleport requests
   - Added lastRequestTime map to track cooldowns
   - Clear user feedback when cooldown is active

### Observations for Future Plugins
- Consider making timeout values configurable via config file
- Particle effects during teleport delay would improve UX
- Economy integration could be an optional feature
- Consider adding permission nodes for different features (e.g., bypass cooldown)
- The unchecked operations warning is worth investigating (likely due to generic Map usage)

### Code Quality Patterns to Follow
- Use static getInstance() pattern for plugin access
- Proper lifecycle management (onLoad, onEnable, onDisable)
- Command registration through Registries.COMMANDS
- Request timeout cleanup with repeating scheduler task
- Movement check during teleport delay is a good anti-abuse measure

---

## BlockLocker (Reviewed 2026-02-03)

### What Went Well
- Good architecture with clear separation (commands, listeners, managers, utils)
- Proper command tree structure
- Good use of permission system
- Proper dimension tracking in location keys
- Thread-safe implementation with ConcurrentHashMap

### Critical Issues Fixed
1. **Missing @EventHandler Annotations (BLOCKER)**: Event handlers never called
   - All event methods in BlockListener had no @EventHandler annotation
   - Plugin loaded successfully but protection never triggered
   - Added annotations to onBlockPlace, onBlockBreak, onPlayerInteract

2. **Dimension ID Missing (MULTI-DIMENSION BUG)**: Location keys incomplete
   - Old format: `worldName:x:y:z` without dimension ID
   - Conflict: Blocks at same coords in different dimensions would conflict
   - New format: `dimensionId:worldName:x:y:z`
   - Updated all methods to use 4-part key format

3. **Bypass Permission Not Implemented**: Missing feature from README
   - README mentioned `blocklocker.bypass` permission
   - Code never checked this permission
   - Added bypass checks in all protection methods
   - Admins with bypass can place/break/interact protected blocks

### Other Issues Fixed
- Removed unused fields (allowRedstone, allowHoppers)
- Added cleanup when players disconnect
- Fixed code duplication in similar methods

### Observations for Future Plugins
- **ALWAYS add @EventHandler to event listener methods**
- Include dimension ID in location keys for multi-dimension support
- Document all permissions mentioned in README
- Implement all features described in documentation

### Code Quality Patterns to Follow
- Use ConcurrentHashMap for thread-safe data structures
- Proper permission checking before operations
- Clear command tree structure
- Good error messages with colored feedback

### Repository Hygiene Issue Fixed
**Build Folder Uploaded to Git**: Added .gitignore and removed build folder
- Problem: Gradle build artifacts (compiled .jar, classes, reports) were uploaded
- Solution: Created .gitignore excluding build/, .gradle/, and other build artifacts
- Removed all build-related files from git tracking (13 files)
- This keeps repository clean and prevents large file bloat

### API Notes
- Permission checking: `player.hasPermission("permission.node") != Tristate.FALSE`
- Location keys should include dimension ID for multi-dimension worlds
- `@EventHandler` annotation is mandatory for event listeners

---

## AnnouncementSystem (Created 2026-02-03)

### What Went Well
- Comprehensive feature set (broadcast, welcome, scheduled, interval)
- Multiple announcement types with color-coded prefixes
- Good command structure with subcommands
- Persistent JSON storage for announcements
- Permission-based access control
- Clear and informative user feedback

### Lessons Learned
1. **Scheduler API Difference**: No `cancelTask()` method in AllayMC
   - Solution: Track task IDs in Set<String>, return false from onRun() to stop
   - This is different from Bukkit's cancel approach

2. **Command Tree Pattern**: `.getRoot().key("subcommand").str("param").exec(...)`
   - Studied SimpleTPA and PlayerHomes to understand pattern
   - More verbose than Bukkit but consistent across AllayMC plugins

3. **Player Type Confusion**: `PlayerJoinEvent.getPlayer()` returns `Player`, not `EntityPlayer`
   - Must convert using `player.getControlledEntity()`
   - EntityPlayer has methods like `sendMessage()`, Player is just data object

4. **Multiple API Differences** from Bukkit:
   - Logging: `getPluginLogger()` instead of `getLogger()`
   - Scheduler: Different task lifecycle management
   - Events: Different event types and registration

### Observations for Future Plugins
- Study existing AllayMC plugins before starting (pattern recognition)
- Always check Player vs EntityPlayer type requirements
- Read AllayMC docs or example plugins for API usage
- Test commands early to verify tree structure works

---

## KitSystem (Reviewed 2026-02-03)

### What Went Well
- Clean structure with proper separation
- Good use of scheduler for delayed kit distribution
- Proper API usage for container access
- Clear command tree structure

### Critical Issues Fixed
1. **PlayerJoinEvent Does Not Exist**: API documentation mismatch
   - Problem: PlayerJoinEvent shown in docs but not in API 0.24.0
   - Solution: Use scheduler task similar to ItemMail's welcome mail
   - Delay kit delivery by 1 tick to ensure player fully joined

2. **Permission System Misunderstanding**: AllayMC uses Tristate
   - Problem: `hasPermission()` returns Tristate (TRUE, FALSE, UNDEFINED)
   - Wrong: `player.hasPermission("perm")` (boolean in Bukkit)
   - Correct: `player.hasPermission("perm") != Tristate.FALSE`

3. **Item Creation API Does Not Exist**: No direct creation from string ID
   - Problem: Cannot create item via `ItemTypes.fromIdentifier("minecraft:diamond")`
   - Solution: Use reflection to find constant in ItemTypes class
   - This is a workaround but works reliably

4. **Missing Gradle Wrapper**: Project incomplete
   - Problem: No gradlew executable for building
   - Solution: Copied gradlew from SimpleTPA
   - Made executable with chmod +x

5. **Container API Misunderstanding**: ContainerTypes.INVENTORY not correct
   - Problem: AllayMC uses container system differently
   - Solution: Use `player.getContainer(ContainerTypes.INVENTORY)` (returns Container)
   - Must manually implement stacking logic (tryAddItem)

6. **YAML Constructor API Change**: CustomClassLoaderConstructor needs LoaderOptions
   - Problem: Constructor signature changed in SnakeYAML
   - Solution: Add LoaderOptions parameter to constructor
   - Required for proper YAML loading

### Code Quality Issues
- HashSet not thread-safe in multi-threaded environment
- Should use ConcurrentHashMap for shared data structures

### Observations for Future Plugins
- API documentation may be outdated - always verify in actual API
- Use reflection as workaround when API methods don't exist
- Copy gradlew from working plugins if missing
- Container access requires special handling in AllayMC
- Always prefer ConcurrentHashMap over HashSet for shared state

---

## PlayerStatsTracker (Reviewed 2026-02-03)

### What Went Well
- Clean code structure with good separation
- Proper use of scheduler for periodic saves
- Thread-safe implementation with ConcurrentHashMap
- Good command tree structure
- Comprehensive statistics tracking (deaths, blocks, etc.)

### Critical Issues Fixed
1. **Missing @EventHandler Annotations**: Event handlers never registered
   - Problem: JoinListener and QuitListener had no @EventHandler
   - Impact: Stats never updated when players joined/left
   - Solution: Added @EventHandler to onJoin and onQuit methods

2. **Player API Type Confusion**: `Player` vs `EntityPlayer`
   - Problem: `Player` type doesn't have getUniqueId() method
   - Impact: Could not track players correctly
   - Solution: Use `entity.getUuid()` from EntityPlayer type

### Observations for Future Plugins
- **ALWAYS add @EventHandler annotation to event listener methods**
- Check method availability on Player vs EntityPlayer types
- Review all event listeners to ensure they're registered

---

## ItemRepair (Reviewed 2026-02-03)

### What Went Well
- Good structure with proper separation
- Clean command tree implementation
- Proper use of container API
- Good user feedback messages

### Critical Issue Fixed
**API Usage Error: Used getMeta()/setMeta() instead of proper durability API**
- Problem: Used `item.getMeta()/setMeta()` which doesn't work for durability
- Impact: Repair functionality completely non-functional
- Root Cause: This is Java Edition pattern, not Bedrock
- Solution: Use correct ItemBaseComponent API
   - `getDamage()` and `setDamage()` for durability
   - `getMaxDamage()` for max durability
- Deleted 80+ lines of unnecessary manual durability table
- This is a **critical difference** between Java and Bedrock editions

### Observations for Future Plugins
- **Bedrock != Java Edition**: Different API patterns for common features
- Use proper component APIs (ItemBaseComponent for damage)
- Always verify if API methods exist for Bedrock before using Java patterns
- Item durability uses Damage component, not Meta

---

## Lessons Learned

### Common Patterns
1. AllayMC plugins extend `Plugin` class
2. Commands are registered via `Registries.COMMANDS`
3. Scheduler uses `Server.getInstance().getScheduler()`
4. Player lookup via `getPlayerByUuid()` pattern with ControlledEntity
5. NBT serialization is key for preserving item state

### Best Practices
1. Use ConcurrentHashMap for thread-safe maps
2. Cleanup expired data with repeating scheduler tasks
3. Provide clear, colored user feedback messages (§ formatting)
4. Validate inputs early in command execution
5. Handle null checks for offline players and world references
6. **ALWAYS add @EventHandler annotation to event listener methods**

### Common Issues to Watch For
1. **Critical**: Gson cannot serialize NbtMap - create custom TypeAdapter
2. Missing cooldown/rate limiting on player actions
3. Hardcoded values that should be configurable
4. NPE risks in player lookups and world references
5. Memory leaks from uncanceled tasks or uncleared maps
6. Incomplete error messages that don't guide users
7. Locale-dependent date formatting
8. **@EventHandler annotation missing** - plugin loads but events never trigger
9. API usage differences between Java and Bedrock Edition

### AllayMC API Gotchas
- `dimension.getWorld()` can return null in some edge cases
- ItemStack uses NBT for state - must serialize/deserialize properly
- ItemStack doesn't expose getMaxStackSize() directly
- Use `ContainerTypes.INVENTORY`, `ContainerTypes.ARMOR`, etc. for container access
- Scheduler tasks: `scheduleRepeating(plugin, task, ticks)`
- `Player` vs `EntityPlayer` distinction in forEachPlayer callbacks
- `player.getUniqueId()` for UUID, not `getLoginData().getUuid()`
- Permissions use `Tristate` enum, not boolean
- Bedrock uses `getDamage()/setDamage()` for durability, not `getMeta()/setMeta()`
- PlayerJoinEvent may not exist - use scheduler workaround

### Repository Hygiene
**Never upload build artifacts to git**
- Always create `.gitignore` file for new projects
- Exclude: `build/`, `.gradle/`, `.idea/`, `*.iml`, `*.class`, `.jar` files
- If already uploaded: remove from tracking and add .gitignore
- Example for BlockLocker (fixed 2026-02-03):
  - Added comprehensive .gitignore
  - Removed 13 build-related files from repository
  - Commit: "chore: add .gitignore and remove build folder from tracking"
