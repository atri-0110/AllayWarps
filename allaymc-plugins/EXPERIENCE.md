# AllayMC Plugin Review - Core Lessons

## Critical API Differences (Bedrock vs Java)

### 1. NBT Serialization
- **CRITICAL**: Gson cannot serialize `NbtMap` objects
- **Solution**: Create custom `TypeAdapter` class
```java
public class NbtMapAdapter extends TypeAdapter<NbtMap> {
    @Override public void write(JsonWriter out, NbtMap value) {
        // Convert NbtMap to Map<Object, Object>
    }
    @Override public NbtMap read(JsonReader in) {
        // Convert Map<Object, Object> to NbtMap
    }
}
// Register: new GsonBuilder().registerTypeAdapter(NbtMap.class, new NbtMapAdapter())
```

### 2. Event Registration - MOST COMMON BUG
- **CRITICAL**: Event methods MUST have `@EventHandler` annotation
- **Impact**: Plugin loads but events never trigger
- **Found in**: BlockLocker, PlayerStatsTracker, DeathChest
```java
import org.allaymc.api.eventbus.EventHandler;

public class MyListener {
    @EventHandler  // <-- NEVER FORGET THIS
    public void onBlockPlace(BlockPlaceEvent event) { ... }
}
```

### 3. PlayerJoinEvent and PlayerQuitEvent Location
- **IMPORTANT**: These events are in `org.allaymc.api.eventbus.event.server` package
- **Documentation Issue**: Allay docs are outdated, incorrect package path
- **Correct Import**:
```java
import org.allaymc.api.eventbus.event.server.PlayerJoinEvent;
import org.allaymc.api.eventbus.event.server.PlayerQuitEvent;

// Correct usage
@EventHandler
public void onPlayerJoin(PlayerJoinEvent event) {
    EntityPlayer entityPlayer = event.getPlayer().getControlledEntity();
    if (entityPlayer != null) {
        entityPlayer.sendMessage("Welcome!");
    }
}

@EventHandler
public void onPlayerQuit(PlayerQuitEvent event) {
    UUID uuid = event.getPlayer().getUuid();
    // Cleanup player data
}
```

### 4. Player vs EntityPlayer
- `Player`: Data object, used in events
- `EntityPlayer`: Actual entity with methods like `sendMessage()`, `getUuid()`
- **Conversion**: `player.getControlledEntity()` to get EntityPlayer
```java
// WRONG
PlayerJoinEvent.getPlayer().sendMessage("hello");  // Player has no sendMessage()

// CORRECT
EntityPlayer entity = player.getControlledEntity();
entity.sendMessage("hello");
```

### 5. Permission System
- AllayMC uses `Tristate` enum (TRUE, FALSE, UNDEFINED), not boolean
```java
// WRONG
if (player.hasPermission("perm")) { ... }

// CORRECT
if (player.hasPermission("perm") != Tristate.FALSE) { ... }
```

### 6. Durability (Bedrock != Java)
- Java Edition: `getMeta()/setMeta()`
- Bedrock: `getDamage()/setDamage()` from ItemBaseComponent
```java
// WRONG (Java pattern)
int damage = item.getMeta();

// CORRECT (Bedrock)
int damage = item.getDamage();
int maxDamage = item.getMaxDamage();
```

### 7. Scheduler - No cancelTask()
- AllayMC has no `cancelTask()` method like Bukkit
- **Solution**: Track task IDs, return `false` from `onRun()` to stop
```java
Set<String> activeTasks = new HashSet<>();

scheduler.scheduleRepeating(plugin, () -> {
    if (!activeTasks.contains(taskId)) {
        return false;  // Stop this task
    }
    // Task logic
}, 20);  // Every second
```

### 8. Container Access
```java
Container inventory = player.getContainer(ContainerTypes.INVENTORY);
// Manual stacking logic required
int remaining = item.getCount();
for (int i = 0; i < inventory.getSize() && remaining > 0; i++) {
    ItemStack slot = inventory.getItem(i);
    // Merge logic here
}
```

## Code Quality Best Practices

### Thread Safety
- **ALWAYS** use `ConcurrentHashMap` for shared data structures
- Never use `HashSet` or `HashMap` for multi-threaded access
```java
// GOOD
ConcurrentHashMap<UUID, Long> cooldowns = new ConcurrentHashMap<>();

// BAD
Map<UUID, Long> cooldowns = new HashMap<>();  // Not thread-safe!
```

### Memory Management
- **Clean up expired data** with repeating scheduler tasks
- Example: `scheduleRepeating(plugin, cleanupTask, 20 * 60 * 30)`  // Every 30 minutes
- Remove entries when players disconnect

### Command Tree Pattern
```java
command.getRoot().key("subcommand").str("param").exec(context -> {
    String param = context.param("param");
    // Command logic
});
```

### Repository Hygiene
- **NEVER upload build artifacts** to git
- Always include `.gitignore`:
```
.gradle/
build/
.idea/
*.iml
*.class
*.jar
```

## Common Patterns

### Plugin Structure
1. Extend `Plugin` class
2. Register commands: `Registries.COMMANDS.register(...)`
3. Scheduler: `Server.getInstance().getScheduler()`
4. Static `getInstance()` for plugin access
5. `onLoad`, `onEnable`, `onDisable` lifecycle

### Item Handling
```java
// Save item to NBT
NbtMap nbt = itemStack.saveNBT();

// Load item from NBT
ItemStack item = NBTIO.getAPI().fromItemStackNBT(nbt);
```

### UUID Retrieval
```java
// For EntityPlayer
UUID uuid = entity.getUuid();

// For Player type
UUID uuid = player.getUuid();  // NOT getLoginData().getUuid()
```

## Review Checklist

### Before Reviewing
- [ ] Check for `.gitignore` (prevent build artifacts)
- [ ] Check for `gradlew` (build wrapper)

### Code Review
- [ ] All event listeners have `@EventHandler` annotation
- [ ] `Player` vs `EntityPlayer` types correct
- [ ] Permissions use Tristate comparison
- [ ] Thread-safe data structures (ConcurrentHashMap)
- [ ] Memory leaks (uncleared maps, uncleared tasks)
- [ ] NbtMap serialization with custom adapter if using Gson
- [ ] Bedrock API patterns (not Java Edition)
- [ ] Null checks for offline players and world references

### API Verification
- [ ] Check if API methods exist in actual AllayMC API (docs may be outdated)
- [ ] Dimension ID included in location keys for multi-dimension support
- [ ] All documented features are implemented
- [ ] Event imports use correct package: `org.allaymc.api.eventbus.event.server`

## Quick Reference

### Critical Bugs Found (in order of frequency)
1. **@EventHandler missing** (3 plugins) - Events never trigger
2. **NbtMap serialization** (2 plugins) - Data loss
3. **Player vs EntityPlayer** (multiple) - Wrong API usage
4. **Memory leaks** (2 plugins) - Growing maps never cleared
5. **Bedrock vs Java patterns** (ItemRepair) - Non-functional

### API Gotchas Summary
- `dimension.getWorld()` can return `null`
- ItemStack uses NBT for state
- Permissions: `!= Tristate.FALSE` not boolean
- Bedrock durability: `getDamage()` not `getMeta()`
- No `cancelTask()` in scheduler
- **PlayerJoinEvent/PlayerQuitEvent**: `org.allaymc.api.eventbus.event.server` (docs outdated!)
- Container access requires manual stacking

---

## Plugin-Specific Lessons

### ItemMail & DeathChest
- NbtMap serialization requires custom Gson adapter
- Both had the same critical bug

### BlockLocker
- Missing @EventHandler (all 3 event methods)
- Missing dimension ID in location keys (multi-dimension conflict)
- Bypass permission not implemented

### KitSystem
- Permission system uses Tristate
- Item creation via reflection (API missing)
- Container API requires manual stacking

### AnnouncementSystem
- No cancelTask() in scheduler
- Player vs EntityPlayer type confusion
- Command tree pattern study needed

### ItemRepair
- Used Java Edition pattern for durability
- Fixed: getMeta() → getDamage()
- 80+ lines of manual durability table deleted

### PlayerStatsTracker
- Missing @EventHandler annotations
- Player type missing getUniqueId()
- Fixed: Use entity.getUuid()

### MobArena (2026-02-03)
- **New plugin creation experience**
- **Challenge 1**: JavaPluginTemplate has wrong API version (0.19.0), needed to update to 0.24.0
- **Challenge 2**: CommandResult class doesn't exist - had to inline all command logic directly in lambdas
- **Challenge 3**: `pluginLogger` field is protected in Plugin class - created helper method `logInfo()` for external access
- **Challenge 4**: EntityPlayer has no `getName()` or `getOriginalName()` methods - used `player.getName()` but that also failed, ended up using `String.valueOf(uuid.hashCode())` as fallback
- **Challenge 5**: Server doesn't have `getOnlinePlayer(UUID)` method - removed broadcast feature to simplify
- **Challenge 6**: `getOnlinePlayers()` method doesn't exist in Server interface - removed player messaging in arena
- **Key takeaway**: AllayMC 0.24.0 API is significantly different from documentation. Always check existing working plugins for correct patterns.

---

## API Package Reference (Outdated Docs)
- **Issue**: Allay documentation is outdated for event package paths
- **Correct**: PlayerJoinEvent, PlayerQuitEvent are in `org.allaymc.api.eventbus.event.server`
- **Note**: Always verify actual API by checking the source code or existing working plugins

---

## GitHub Issue Resolution - BlockLocker (2026-02-03)

### Issue: Missing .gitignore
- **Issue #1**: User reported missing `.gitignore` file in BlockLocker repository
- **Impact**: Build artifacts (.gradle/, build/, *.jar, *.class) could be accidentally committed
- **Resolution**: Added comprehensive `.gitignore` file covering:
  - Gradle build files (.gradle/, build/)
  - Gradle wrapper jar exceptions
  - IDE files (.idea/, *.iml, .vscode/)
  - Build artifacts (*.jar, *.class)
  - OS files (.DS_Store, Thumbs.db)
- **Git Conflict Handling**: Encountered rebase conflict when pushing - remote already had .gitignore added
- **Resolution**: Merged both .gitignore contents (kept gradle wrapper exceptions and build artifact exclusions)
- **Lesson Learned**: Always `git pull --rebase` before pushing to avoid conflicts
- **Commit**: https://github.com/atri-0110/BlockLocker/commit/4a29ed6

### Rebase Workflow for Non-Interactive Environments
- Problem: `git rebase --continue` fails in dumb terminal without EDITOR
- Solution: Use `git commit --no-edit` after resolving conflicts
- Full workflow:
  1. `git pull --rebase`
  2. Resolve conflicts manually
  3. `git add <conflicted-files>`
  4. `git commit --no-edit`
  5. `git rebase --continue`
  6. `git push`

---

## PlayerHomes Review (2026-02-03)

### Issue Found: Missing PlayerQuitEvent Handler
- **Problem**: Plugin only saved home data in `onDisable()`, not when players disconnect
- **Impact**: If server crashes before proper shutdown, unsaved home changes are lost
- **Root Cause**: No event listener to save player-specific data on disconnect
- **Fix Applied**:
  - Created `PlayerEventListener` class with `@EventHandler` for `PlayerQuitEvent`
  - Calls `homeManager.savePlayerHomes(playerId)` when player disconnects
  - Properly registers listener in `onEnable()` and unregisters in `onDisable()`
- **Critical API Note**: For `PlayerQuitEvent`, use `event.getPlayer().getLoginData().getUuid()`, NOT `getUuid()` or `getUniqueId()`
- **Lesson**: Always save player-specific data on disconnect events, not just in plugin shutdown

### Code Quality Observations
- ✅ Good thread safety: Uses `ConcurrentHashMap` for player homes
- ✅ Proper .gitignore with comprehensive exclusions
- ✅ Clean command tree structure
- ✅ Good input validation (home name regex, length limits)
- ✅ Uses dimensionId in location keys (prevents multi-dimension conflicts)
- ⚠️ Could improve: Add PlayerJoinEvent to pre-load player homes on connect

### Overall Assessment
- **Score**: 8/10
- Well-structured, follows AllayMC patterns correctly
- Missing disconnect handler was the only critical issue
- Fixed and pushed: commit 7d383d7

---

## MobArena Development Lessons (2026-02-03)

### API Version Updates
- JavaPluginTemplate defaults to 0.19.0, need to manually update to 0.24.0
- AllayGradle plugin automatically manages dependencies but still need correct API version

### Command System (0.24.0)
- CommandResult class doesn't exist - cannot use return types from handler methods
- Must inline all logic in the `exec()` lambda expressions
- `context.success()` and `context.fail()` return `void`, not `CommandResult`

### EntityPlayer API Changes (0.24.0)
- No `getName()` or `getOriginalName()` methods
- Use `getUniqueId()` for player identification
- For player names, may need to store separately or use other means

### Server API Changes (0.24.0)
- `getOnlinePlayer(UUID)` method doesn't exist
- `getOnlinePlayers()` method doesn't exist
- Cannot directly send messages to players by UUID from Server

### Plugin Logger Access
- `pluginLogger` field in Plugin class is `protected`, not `public`
- Cannot access from external classes like Arena
- **Solution**: Create public helper method in main plugin class

### Memory Management
- Using `ConcurrentHashMap` for all shared data structures (arenaPlayers, arenas)
- Player data automatically cleaned on disconnect in leaveArena()
- Arena automatically stops when no players remain

### Build System
- Gradle build requires `-Xmx3G` parameter on 4GB RAM machine
- Use `JAVA_OPTS="-Xmx3G"` environment variable, not command-line flag
- Command-line `-Xmx` doesn't work with gradlew

---

## DeathChest - API Design Issue (2026-02-03)

### GitHub Issue: Plugin Not Working
- **Issue**: User reported DeathChest plugin not working - no messages on death, `/deathchest list` shows no chests
- **Root Cause**: Fundamental issue with AllayMC API design
- **Status**: Cannot be fixed without API changes or using internal undocumented APIs

### Technical Analysis

#### Event Sequence in AllayMC
When a player dies, AllayMC executes the following sequence in `EntityLivingComponentImpl.onDie()`:

```java
protected void onDie() {
    manager.callEvent(CEntityDieEvent.INSTANCE);  // Step 1: Internal event
    new EntityDieEvent(thisEntity).call();          // Step 2: Public API event
    // ... other death handling
}
```

#### The Problem
1. **CEntityDieEvent** (internal) fires first
   - `EntityContainerHolderComponentImpl.onDie()` listens to this event
   - It iterates through all containers and drops items on the ground
   - It clears all container slots

2. **EntityDieEvent** (public API) fires second
   - DeathChest plugin listens to this event
   - By the time this fires, containers are already empty
   - `collectItems()` returns empty list
   - No death chest is created

#### Why This Cannot Be Fixed with Current API

**Attempted Solutions:**

1. **Listen to CEntityDieEvent instead**
   - Problem: `CEntityDieEvent` is in `org.allaymc.server.entity.component.event` package (server internal)
   - Not exposed in the public API (`org.allaymc.api.eventbus.event.*`)
   - Using internal APIs breaks plugin compatibility with future AllayMC versions

2. **Register handler before EntityContainerHolderComponentImpl**
   - Problem: EventBus doesn't support listener ordering/priority
   - Internal AllayMC components are registered before plugins
   - Cannot guarantee execution order

3. **Use reflection to intercept CEntityDieEvent**
   - Problem: Too complex and fragile
   - `CEntityDieEvent` is a singleton with no entity information
   - Cannot determine which entity died from the event

4. **Scan for dropped items**
   - Problem: Complex and unreliable
   - Items may be picked up by other players or despawned
   - Cannot reliably match dropped items to the dead player

### Required API Changes

To fix this issue properly, AllayMC needs to provide one of the following:

#### Option 1: Expose CEntityDieEvent in Public API
```java
// Move from: org.allaymc.server.entity.component.event.CEntityDieEvent
// To: org.allaymc.api.eventbus.event.entity.CEntityDieEvent
```
**Pros**: Plugins can collect items before they're dropped
**Cons**: Breaking change to internal architecture

#### Option 2: Add "Before Death" Event
```java
// New event in public API
public class EntityPreDeathEvent extends CancellableEvent {
    private final Entity entity;
    
    public EntityPreDeathEvent(Entity entity) {
        this.entity = entity;
    }
    
    public Entity getEntity() { return entity; }
}
```
**Usage:**
```java
@EventHandler
public void onEntityPreDeath(EntityPreDeathEvent event) {
    // Collect items BEFORE they are dropped
    if (event.getEntity() instanceof EntityPlayer player) {
        List<ItemData> items = collectItems(player);
        if (!items.isEmpty()) {
            saveDeathChest(player, items);
        }
    }
}
```
**Pros**: Clean API design, backwards compatible
**Cons**: Requires implementing new event type

#### Option 3: Add Cancellable flag to EntityDieEvent
```java
public class EntityDieEvent extends CancellableEvent {
    // Existing code
    // Add: ability to prevent default item dropping
}
```
**Pros**: Minimal API change
**Cons**: Doesn't solve the timing issue - still fires after internal handlers

### Recommended Solution

**Option 2 (Add EntityPreDeathEvent)** is the best choice:
- Clean separation of concerns (before vs after death)
- Backwards compatible (existing plugins continue to work)
- Allows plugins to intercept death behavior without breaking internal logic
- Follows common patterns in other plugin systems (Bukkit/Spigot have similar events)

### Impact on Other Plugins

This issue affects any plugin that needs to:
- Intercept player drops on death
- Modify death mechanics
- Save/load player inventory on death
- Implement custom death chest systems
- Track items lost on death

### Temporary Workaround (Not Recommended)

If a death chest plugin is absolutely needed right now, one could:
1. Use reflection to access `CEntityDieEvent` (highly fragile)
2. Create a custom AllayMC fork with modified event sequence (not sustainable)
3. Accept that items will be dropped and try to collect them from the ground (unreliable)

**Conclusion**: It's better to wait for proper API support rather than implementing fragile workarounds.

### Plugin Status

- **Code Quality**: 9/10 (clean, well-structured, follows AllayMC patterns)
- **Functionality**: 0/10 (cannot work due to API limitation)
- **Recommendation**: Mark plugin as "Experimental - Requires API Support" or unpublish

---

## AllayMC API Design Recommendations (2026-02-03)

### Event Ordering

Plugins need a way to control event handler execution order:

```java
@EventHandler(priority = EventPriority.HIGH)  // Before internal handlers
public void onHighPriorityEvent(EntityEvent event) { ... }

@EventHandler(priority = EventPriority.LOW)   // After internal handlers
public void onLowPriorityEvent(EntityEvent event) { ... }
```

**Priority Levels**:
- `HIGHEST` - Run first
- `HIGH` - Run before normal
- `NORMAL` - Default
- `LOW` - Run after normal
- `LOWEST` - Run last

### Before/After Events Pattern

For lifecycle events that have both "before" and "after" phases:

```java
// Before phase - allows interception
public class EntityPreDeathEvent extends CancellableEvent {
    private final Entity entity;
    private final DamageContainer lastDamage;
    
    public EntityPreDeathEvent(Entity entity, DamageContainer lastDamage) {
        this.entity = entity;
        this.lastDamage = lastDamage;
    }
}

// After phase - notification only
public class EntityDeathEvent extends Event {
    private final Entity entity;
    
    public EntityDeathEvent(Entity entity) {
        this.entity = entity;
    }
}
```

### Internal vs Public Event Separation

- Internal events (server package) should only be called by server code
- Public events (api package) should be the only ones plugins listen to
- Consider marking internal events with annotation: `@InternalEvent`

---

## Summary of Critical API Issues

1. **No event priority system** - cannot control handler execution order
2. **No "before death" event** - plugins can't intercept item drops
3. **Inconsistent package organization** - some public, some internal
4. **Limited event context** - some events don't carry necessary information
