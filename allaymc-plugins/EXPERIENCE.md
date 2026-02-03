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
public class MyListener {
    @EventHandler  // <-- NEVER FORGET THIS
    public void onBlockPlace(BlockPlaceEvent event) { ... }
}
```

### 3. Player vs EntityPlayer
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

### 4. Permission System
- AllayMC uses `Tristate` enum (TRUE, FALSE, UNDEFINED), not boolean
```java
// WRONG
if (player.hasPermission("perm")) { ... }

// CORRECT
if (player.hasPermission("perm") != Tristate.FALSE) { ... }
```

### 5. Durability (Bedrock != Java)
- Java Edition: `getMeta()/setMeta()`
- Bedrock: `getDamage()/setDamage()` from ItemBaseComponent
```java
// WRONG (Java pattern)
int damage = item.getMeta();

// CORRECT (Bedrock)
int damage = item.getDamage();
int maxDamage = item.getMaxDamage();
```

### 6. Scheduler - No cancelTask()
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

### 7. PlayerJoinEvent May Not Exist
- API docs show `PlayerJoinEvent`, but it may not exist in your version
- **Workaround**: Use delayed scheduler task
```java
// Delay kit/item delivery by 1 tick to ensure player fully joined
scheduler.scheduleDelayed(plugin, () -> {
    EntityPlayer player = Server.getInstance().getPlayerByUuid(uuid);
    if (player != null) { giveKit(player); }
}, 1);
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
- PlayerJoinEvent may not exist

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
- PlayerJoinEvent doesn't exist (use scheduler workaround)
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
