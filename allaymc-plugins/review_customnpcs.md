# CustomNPCs Plugin Review (2026-02-05)

## Plugin Overview
CustomNPCs is a data management system for storing NPC configurations with commands to create, list, and interact with NPC definitions. However, it's **fundamentally incomplete** - it stores NPC data but never actually spawns NPC entities in the game world or handles real in-game interactions.

## Critical Issues Found

### 1. MISSING: No NPC Entity Spawning System
- **Problem**: The plugin only stores NPC data (JSON) but never creates/spawns actual Entity objects in the world
- **Impact**: Even after creating an NPC with `/npc create`, nothing appears in the game world - no entities are spawned
- **Root Cause**: No implementation of entity spawning logic using AllayMC's Entity API
- **Required Fix**:
  ```java
  // Need to implement NPC spawning in NPCManager
  public void spawnNPC(NPCData npcData) {
      // Parse location string
      String[] parts = npcData.getLocation().split(":");
      World world = Server.getInstance().getWorld(parts[0]);
      Dimension dimension = world.getDimension(Integer.parseInt(parts[1]));

      // Create and spawn entity
      // Note: AllayMC may not have a direct "NPC" entity type
      // May need to use EntityHuman or create custom entity
      EntityPlayer npcEntity = new EntityHuman(...); // Pseudocode
      npcEntity.setLocation(...);
      dimension.addEntity(npcEntity);

      // Track spawned entity
      spawnedEntities.put(npcData.getId(), npcEntity);
  }
  ```
- **Status**: BLOCKER - Core functionality doesn't work at all

### 2. MISSING: EntityInteractEvent Listener
- **Problem**: The `NPCEventListener` class exists but is COMPLETELY EMPTY - no event handlers defined
- **Impact**: Players cannot interact with NPCs even if they were spawned - no event listener for detecting right-click interactions
- **Root Cause**: Event listener class is a stub and never registered in main plugin
- **Current State**:
  ```java
  public class NPCEventListener {
      private final CustomNPCs plugin;
      private final ConcurrentHashMap<java.util.UUID, Long> cooldowns = new ConcurrentHashMap<>();

      public NPCEventListener(CustomNPCs plugin) {
          this.plugin = plugin;
      }
      // NO EVENT HANDLERS - EMPTY CLASS!
  }
  ```
- **Required Fix**:
  ```java
  @EventHandler
  public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
      EntityPlayer player = event.getPlayer();
      Entity interacted = event.getEntity();

      // Find NPC data for this entity
      NPCData npc = findNPCByEntity(interacted);
      if (npc != null) {
          event.setCancelled(true); // Prevent default interaction

          if (npc.getType().equals("dialog")) {
              for (String message : npc.getMessages()) {
                  player.sendMessage("[NPC] " + npc.getName() + ": " + message);
              }
          } else if (npc.getType().equals("command")) {
              executeCommands(player, npc.getCommands());
          }
      }
  }
  ```
- **Status**: BLOCKER - Core feature doesn't work

### 3. MISSING: Event Listener Registration
- **Problem**: `NPCEventListener` is never registered in `CustomNPCs.onEnable()`
- **Impact**: Even if event handlers were added, they wouldn't fire because listener isn't registered
- **Current Code** (CustomNPCs.java):
  ```java
  @Override
  public void onEnable() {
      this.npcManager = new NPCManager(this);
      // ... load data
      var command = new NPCCommand(npcManager);
      Registries.COMMANDS.register(command);

      // NO: Server.getInstance().getEventBus().registerListener(eventListener);
  }
  ```
- **Required Fix**:
  ```java
  private NPCEventListener eventListener;

  @Override
  public void onEnable() {
      this.npcManager = new NPCManager(this);
      this.eventListener = new NPCEventListener(this);

      npcManager.loadData();
      Server.getInstance().getEventBus().registerListener(eventListener);

      var command = new NPCCommand(npcManager);
      Registries.COMMANDS.register(command);
  }

  @Override
  public void onDisable() {
      Server.getInstance().getEventBus().unregisterListener(eventListener);
      // ... rest of disable
  }
  ```
- **Status**: BLOCKER

### 4. MISLEADING: "Interact" Command is Manual Test, Not Real Interaction
- **Problem**: The `/npc interact <id>` command is a manual test tool, not how players interact with NPCs
- **Impact**: README says "Players can interact with NPCs using: `/npc interact welcome_wizard`" but this is only for admins testing NPCs
- **Current Code** (NPCCommand.java):
  ```java
  .key("interact")
  .str("id")
  .exec(context -> {
      // This is MANUAL interaction via command, not in-game right-click
      if (npc.getType().equals("dialog")) {
          // Show messages
      }
      // ...
  })
  ```
- **Reality**: Players should interact by right-clicking on NPC entities in the world, not by typing a command
- **Status**: Documentation issue - feature works as designed, but documentation is misleading

### 5. UNUSED: cooldowns Map in Event Listener
- **Problem**: `ConcurrentHashMap<java.util.UUID, Long> cooldowns` is declared but never used
- **Impact**: Memory waste (though minor)
- **Fix**: Remove unused field or implement cooldown logic if needed for spam prevention

## Code Quality Assessment

### ✅ Strengths

1. **Clean Command System**
   - Well-structured command tree with all subcommands: create, remove, list, info, interact, setmessages, setcommands, reload, save
   - Good input validation: ID uniqueness check, type validation (dialog/command)
   - Uses `context.getResult(n)` for parameter access (correct pattern)
   - Returns `context.success()` and `context.fail()` appropriately

2. **Thread-Safe Data Storage**
   - Uses `ConcurrentHashMap` for NPC data storage
   - Proper synchronization on file I/O operations
   - No race conditions in NPC CRUD operations

3. **Persistence Layer**
   - Uses Gson for JSON serialization with pretty printing
   - Handles file I/O with try-with-resources
   - Creates data directories automatically
   - Saves data immediately after modifications

4. **Proper Location Serialization**
   - Stores world name, dimension ID, x/y/z coordinates, yaw, pitch
   - Supports cross-dimension storage
   - Good for future NPC spawning implementation

5. **Good Documentation**
   - Comprehensive README with command tables
   - Clear usage examples
   - Future plans section showing development roadmap
   - Proper licensing

6. **Build Configuration**
   - Proper `.gitignore` covering all build artifacts
   - Correct AllayGradle configuration with API version 0.24.0
   - Uses Lombok for clean data classes
   - Builds successfully with `-Xmx3g` flag

### ⚠️ Issues Found

1. **Core functionality completely missing** (see Critical Issues above)
2. **Event listener is empty and never registered**
3. **No entity spawning logic**
4. **No real interaction handling**
5. **Unused `cooldowns` map**

### ✅ No Other Bugs

1. **Command registration** ✓ (uses Registries.COMMANDS.register)
2. **Thread safety** ✓ (ConcurrentHashMap)
3. **API imports** ✓ (correct AllayMC 0.24.0 patterns)
4. **Build system** ✓ (successful)

## API Compatibility Notes

The plugin doesn't have much API usage due to being incomplete. What exists:

- **Command registration**: `Registries.COMMANDS.register(command)` - CORRECT
- **EntityPlayer access**: `context.getSender() instanceof EntityPlayer` - CORRECT
- **Location methods**: Uses `player.getLocation().x()`, `.y()`, `.z()` - CORRECT (lowercase)
- **World access**: `player.getWorld()`, `player.getDimension()` - CORRECT

However, the plugin needs the following API calls to be functional:
- **Entity spawning**: Need to use AllayMC's Entity API (EntityHuman or custom entity type)
- **EntityInteractEvent**: Need to listen to this event for real interaction
- **EventBus**: Need to register event listeners

## Fundamental Architecture Problem

This plugin is essentially a **database + admin commands** but not an actual **NPC system**. The architecture is missing the core components:

### What It Has:
- ✅ Data storage (JSON)
- ✅ Admin commands to manage NPC data
- ✅ Serialization/deserialization

### What It's Missing:
- ❌ Entity spawning logic
- ❌ Entity tracking (which Entity corresponds to which NPCData)
- ❌ Event handling for interactions
- ❌ Actual in-game NPC presence

### What It Needs to Work:

1. **Entity Spawning System**:
   ```java
   // In NPCManager
   private final Map<String, Entity> spawnedEntities = new ConcurrentHashMap<>();

   public void spawnAllNPCs() {
       for (NPCData npc : npcs.values()) {
           spawnNPC(npc);
       }
   }

   private void spawnNPC(NPCData npc) {
       // Parse location from npc.getLocation()
       // Create entity (EntityHuman or custom entity)
       // Set entity properties (name tag, invulnerable)
       // Spawn entity in dimension
       // Track entity: spawnedEntities.put(npc.getId(), entity)
   }
   ```

2. **Entity-NPC Mapping**:
   ```java
   private final Map<UUID, String> entityToNpcMap = new ConcurrentHashMap<>();

   @EventHandler
   public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
       Entity entity = event.getEntity();
       String npcId = entityToNpcMap.get(entity.getUniqueId());
       if (npcId != null) {
           NPCData npc = npcs.get(npcId);
           handleInteraction(event.getPlayer(), npc);
           event.setCancelled(true);
       }
   }
   ```

3. **NPC Appearance**:
   - Need to set entity skin (skinData field exists but not used)
   - Need to set entity name tag (showNameTag field exists but not used)
   - Need to set entity invulnerability (isInvulnerable field exists but not used)

4. **NPC Metadata Tracking**:
   - Which EntityUUID belongs to which NPC ID
   - NPC entities need to persist across server restarts
   - Need to respawn NPCs on plugin reload

## Unique Design Patterns

None significant - plugin is too incomplete to have meaningful patterns.

## Overall Assessment

- **Code Quality**: 7/10 (clean structure, good patterns where implemented)
- **Functionality**: 0/10 (core features completely missing - NO actual NPCs in-game)
- **API Usage**: N/A (insufficient to assess - needs entity spawning and event handling)
- **Thread Safety**: 10/10 (excellent ConcurrentHashMap usage)
- **Documentation**: 6/10 (comprehensive but misleading about "interact" command)
- **Build Status**: ✅ Successful
- **Recommendation**: INCOMPLETE - Cannot be used without major implementation

### Summary

This plugin is a **foundation** for an NPC system, not a functional NPC system itself. It has:
- Good data storage layer
- Good admin command interface
- Proper persistence

But lacks:
- Entity spawning
- Real interaction handling
- Any in-game presence of NPCs

The plugin needs significant development to become functional:
1. Implement entity spawning using AllayMC's Entity API
2. Add EntityInteractEvent listener for real interactions
3. Implement entity-to-NPC mapping
4. Respawn NPCs on server startup
5. Handle NPC appearance (name tags, skins)
6. Implement command execution for command-type NPCs

### Can This Be Fixed?

**NO**, not within the scope of a simple bug fix. This is a **missing feature implementation**, not a bug. The plugin would need:

- **Entity spawning API knowledge**: Understanding how AllayMC creates and manages entities
- **Entity-NPC mapping**: Tracking which in-game entities correspond to which NPC configurations
- **Event handling**: Properly intercepting player interactions with entities
- **Persistence of entity state**: Respawning NPCs on server restarts

This is essentially **50% of a plugin** - the data layer is complete, but the entity layer is missing entirely.

### Estimated Work Required

To make this plugin functional:
1. **Entity spawning system**: ~200-300 lines of code
2. **Event handling system**: ~100-150 lines of code
3. **Entity-NPC mapping**: ~50-100 lines of code
4. **NPC appearance management**: ~100-150 lines of code
5. **Testing and bug fixes**: ~100-200 lines of code

**Total**: ~550-900 lines of additional code

### Recommendation

**Status**: Mark plugin as **INCOMPLETE / PROTOTYPE**

The plugin should either:
1. **Be completed** with the missing entity spawning and interaction systems
2. **Be marked as WIP** with clear documentation that it's a data storage foundation, not a functional NPC system
3. **Be renamed** to something like "NPCDataManager" to reflect its actual functionality

### Lessons Learned

1. **Core functionality must be implemented** - Data storage is not enough; entities must be spawned
2. **Event listeners must be registered** - An empty listener class does nothing
3. **README should reflect actual features** - Don't document features that don't work
4. **Entity APIs are complex** - Creating and managing entities requires significant code
5. **Entity-NPC mapping is essential** - Need to track which entity belongs to which NPC data
6. **Persistence of entities matters** - NPCs must respawn on server restarts
7. **Interaction handling requires events** - Players should right-click entities, not type commands
8. **Plugin development has layers** - Data layer is easy, entity layer is hard

### Commit Details

**No commits made** - Plugin is too incomplete for simple bug fixes. This requires major feature implementation, not bug fixing.

---

## Comparison with Functional NPCs Plugins

| Aspect | CustomNPCs | Functional NPC Plugin (e.g., Citizens) |
|--------|-------------|---------------------------------------|
| Entity spawning | ❌ Missing | ✅ Implemented |
| Interaction handling | ❌ Missing | ✅ EntityInteractEvent |
| Entity tracking | ❌ No mapping | ✅ UUID-based mapping |
| Data storage | ✅ JSON | ✅ Usually YAML/JSON |
| Admin commands | ✅ Complete | ✅ Complete |
| In-game presence | ❌ None | ✅ Visible entities |
| Real player interaction | ❌ Command-based | ✅ Right-click based |

CustomNPCs is essentially a **database with admin commands**, not an NPC system.
