# History

A faithful chronological record of how this mod was built, including every
user request (verbatim where possible, in Russian), the decisions taken, the
bugs encountered, and the fixes. Use this as context for any AI agent or
human collaborator picking up the project later.

The current author was a Devin AI session working with GitHub user
`gghyrmrwf`. The session began with an empty repository and reached
**Phase 2.0 armor gate**, with a **post-1.14 fix pass** in response to user
feedback.

---

## Communication style observed

- The user (`gghyrmrwf`) communicated in **Russian**. All design
  decisions, parameters, and feedback were in Russian. Code, commit messages,
  and PR descriptions are in English.
- The user's iteration style: short directive messages → I deliver → user
  tests → user reports issues or asks for next thing.
- The user explicitly asked, mid-session, to **stop generating .txt
  changelogs** for each phase ("не нужно каждый раз делать txt файл с
  описанием что ты изменил"). After that point, only code + commits.
- The user values **pragmatism over completeness** — they prefer "here's a
  working .jar, with caveats" over "here's the perfect plan, no code yet".
- The user is open to AI-suggested architectural advice but wants to make
  the design calls themselves.

---

## Timeline

### Phase 0 — Mod scaffold

**Request:** Set up an empty Forge 1.20.1 mod called `glebthanwolves`.

**Delivered:** Mod loads in dev client, shows up in F3 Mods screen as
`Gleb Than Wolves 1.0.0`. Empty otherwise.

**Commit:** `113cb11` — `Phase 0: Forge 1.20.1 MDK scaffold`

**PR:** #1 https://github.com/gghyrmrwf/gleb-than-wolves/pull/1

---

### Phase 1.1 — Primitive bushcraft (initial design conversation)

**Request:** "Make Minecraft start brutal — no breaking trees with bare
hands. Player should pick grass, make fiber, make a primitive axe, then
chop trees."

**Design decisions (proposed by me, accepted by user):**
- Plant fiber drops only from tall grass / large fern (15%).
- Cordage = 2 fiber.
- Primitive axe = `[Flint][Cordage] / [_][Stick]`.
- 2×2 inventory crafting (no workbench needed) so player can craft the
  axe before they have a workbench.

**Bug found mid-implementation:** Initial recipe was 3×3, requiring a
workbench. User noticed and asked "could it be made without a workbench?"
Reduced to 2×2.

**Commits:**
- `c0b805a` — Phase 1.1 main work
- `191ab9b` — fix: 2×2 recipes, lower fiber/sapling rates

**PR:** #2 https://github.com/gghyrmrwf/gleb-than-wolves/pull/2 (this PR
became the cumulative branch that everything else stacks onto)

---

### Phase 1.2 — Wood chunks

**Request:** "Make getting a workbench less trivial. Maybe trees shouldn't
drop logs directly?"

**Design:**
- Trees drop 1–2 wood chunks instead of logs.
- 2 wood chunks → 1 plank (2×2 shapeless).
- Primitive axe durability lowered to 8 (was 30).
- Math: ~8 chunks needed for a workbench (4 planks), needing 4–6 trees
  and at least 1 axe.

**Commit:** `ebc0039`

---

### Phase 1.3 — Hardcore world

**Request:** "Make the world more hostile. Day shorter, raw food bad,
night more dangerous, mobs tougher."

**Design (5 mechanics):**
1. Daytime ×1.5 speed.
2. Raw meat/fish → Hunger I 12 sec.
3. +30% mob spawns at night.
4. Mobs +15% HP / +10% damage.

**Commit:** `7e3bbd3`

---

### Phase 1.4 — Player nerfs

**Request:** "Now nerf the player. 5 hearts. No sprint. Sleep should
sometimes fail. Golden apples shouldn't be a panic button."

**Design:**
1. HP cap 10 (5 hearts).
2. No sprint via clamping foodLevel = 6.
3. Zombies +20% speed.
4. Phantoms still spawn after sleep.
5. 20% sleep fail.
6. Golden apple — strip Regen / Absorption / Resistance / FireRes effects.

**Bug found in v1:** Clamping `foodLevel = 6` every tick blocked vanilla
regen entirely, AND had client-side prediction issues. User reported
"feels weird, sprint sometimes flickers".

**Fix (v2):** Switched to "drain hunger ~6× faster while foodLevel > 6".
Vanilla regen restored (player can heal again after eating heavily).

**Commits:**
- `56c7bde` — Phase 1.4 v1
- `1f64f6c` — Phase 1.4 v2 (regen-friendly sprint blocker)

---

### Phase 1.5 — Raw food damages

**Request:** "Raw food should also hurt. Not just hunger debuff."

**Design:** 1 raw fish/meat eaten = -0.5 hearts (1 HP) generic damage.
Ignores armor.

**Commit:** `9094727`

---

### Phase 1.6 — Environment & combat

**Request:** "5 small things: fall hurts more, rain hurts under sky,
night cold without warmth, zombies grab and slow you, skeleton arrows
sting more."

**Design:**
1. Fall ×1.5.
2. Rain -0.5 HP / 10 sec under open sky.
3. Cold nights -0.5 HP / 30 sec when block-light ≤ 7.
4. Zombie hit → 30% Slowness II 3 sec.
5. Skeleton arrows ×1.5 damage.

**Commit:** `a408970`

---

### Phase 1.7 — Aggressive iron golems + no trading

**Request:** "Iron golems should attack the player on sight. Can't trade
with villagers."

**Bug 1 (v1):** Implemented golem aggro via
`targetSelector.addGoal(NearestAttackableTargetGoal)`. Tested in dev:
**didn't work**. Vanilla's `canAttack(player)` returns false for
player-built golems (`isPlayerCreated() == true`).

**Fix attempt 1:** Add `setPlayerCreated(false)` on spawn. **Still didn't
work** — village golems also weren't attacking. Some vanilla check must be
filtering the goal anyway.

**Fix v2 (works):** Skipped the goal system entirely. Use `LivingTickEvent`
on every Iron Golem, every 20 ticks set `golem.setTarget(nearestPlayer)`
directly. The vanilla `MeleeAttackGoal` picks up the target and walks to
the player.

**User-reported bug:** "Голем меня не бьёт" → Fix v2 above.
**User-reported bug:** "Даже големы в деревне меня не трогают" → also fixed
by Fix v2.

**Verified in dev client:** Spawned a golem, switched to survival, golem
killed me with the death message "Dev was slain by Iron Golem".

**Trading:** `PlayerInteractEvent.EntityInteract` cancelled if right-click
on `AbstractVillager`. Side effect (flagged to user, awaiting decision):
leads, name tags, zombie villager curing also blocked.

**Commits:**
- `3eddd6a` — Phase 1.7 v1
- `a3297f4` — Phase 1.7 fix (LivingTickEvent based)

---

### Phase 1.8 — Wild wolves attack

**Request:** "Хочу чтобы волки атаковали меня даже если я их не трогал."
(Want wolves to attack me even if I haven't touched them.)

**Design:** Same pattern as iron golems — `LivingTickEvent` on Wolf,
`!isTame()`, every 20 ticks setTarget(nearest player) within 16 blocks.
Tamed wolves stay loyal.

**Commit:** `0f515b2`

---

### Phase 1.9 — Boats & oxygen

**Request:** "Лодки в 2 раза медленнее, кислород тратится в 2 раза быстрее
под водой."

**Design:**
1. Each player tick in a boat: `dm.x *= 0.91`, `dm.z *= 0.91`. Vanilla
   `v_max ≈ 0.40`, ours ≈ 0.20.
2. Underwater without Water Breathing / turtle helmet: extra `airSupply -= 1`
   per tick. Total drain 2/tick. Bar empties in 7.5 sec instead of 15.

**Commit:** `757a305`

---

### Architectural discussion (after Phase 1.9)

**User question:** "Есть идеи как предугадать всё? В будущем я хочу заменить
почти все предметы и крафты, чтобы было супер сложно, можно сказать
перестроить майнкрафт. Но в Майнкрафте слишком много механик — найти
предметы в сундуках, выбить с мобов, купить у жителя — всё это предусмотреть
почти невозможно. Я хочу знать твои идеи."

**My response (long-form, paraphrased — full text in `ROADMAP.md`):**
The 7 architectural principles for redesigning Minecraft:
1. Replace, don't delete.
2. Layer-based progression via tags.
3. Datapack is the primary tool (90% of the work is JSON).
4. Forge events for dynamic surfaces.
5. Single chokepoint for banned items (`EntityItemPickupEvent`).
6. Transmutation instead of deletion.
7. Top-down design of the craft tree.

**User's response:** "Хорошо учту это" — acknowledged but didn't ask to
start Phase 2 yet.

---

### Brainstorm (44 mechanics offered, 25 picked)

**My message:** Listed 44 small/medium mechanics in 9 categories, each
tagged with difficulty (★, ★★, ★★★).

**User picked:** `{1, 2, 3, 4, 6, 7, 9, 10, 12, 13, 14, 15, 17, 18, 19, 20,
21, 23, 24, 27, 36, 37, 39, 40, 41}` (25 mechanics).

**User instruction:**
> "Реализуй это, но понятное дело не за один раз, группируй и делай по
> очереди, но не слишком много групп, примерно по 5 механик за раз, делай
> внимательно и если это возможно делай тесты."

(Implement this, but obviously not all at once, group and do them in turn,
not too many groups, roughly 5 mechanics per group, do it carefully and
if possible run tests.)

**My grouping (proposed):**
- Phase 1.10 (Movement): 1, 2, 3, 4, 6, 7
- Phase 1.11 (Predators): 9, 10, 12, 13, 14
- Phase 1.12 (Environmental hazards): 15, 17, 18, 19, 20
- Phase 1.13 (Perception + physiology): 21, 23, 24, 27
- Phase 1.14 (World + items): 36, 37, 39, 40, 41

**User feedback during execution:**
> "Не нужно каждый раз делать txt файл с описанием что ты изменил."

(No need to make a txt file every time describing what you changed.)
**Action:** Stopped generating per-phase changelog .txt files after this.

---

### Phase 1.10 — Movement complications

**Implemented:** All 6 picks (1, 2, 3, 4, 6, 7). See `CHANGELOG.md`.

**Commit:** `7d6cc3e`

---

### Phase 1.11 — Predators (FIRST CUT, contained a numbering error)

**What I implemented:**
1. Endermen 16-block aggro
2. Silent creepers 15%
3. Husk replacing zombie 10%
4. Ghast extra fireball
5. Headless creeper +radius

**Bug (caught later):** Endermen aggro is brainstorm item **#8**, not #10.
The user picked **#10 (witches everywhere)**, not #8. I implemented the
wrong one. See "Post-1.14 fix" below.

**Commit:** `f28780b`

---

### Phase 1.12 — Environmental hazards

**Implemented:** All 5 picks (15, 17, 18, 19, 20).

**Commit:** `4150a7c`

---

### Phase 1.13 — Perception + physiology (CONTAINED RAIN-FOG ERROR)

**What I implemented:**
1. Rain blindness pulses (was item #21 in my brainstorm = "Rain fog")
2. Zombie infection 15% Hunger II 5 min (item #23)
3. Death fever (item #24)
4. Sleep deprivation (item #27)

**Compile bug found mid-implementation:** Pattern matching on
`if (!(event.getEntity() instanceof Player player) || event.isEndConquered())`
hit a Java 16+ pattern type-mismatch error. **Fix:** restructured to first
extract `Player player = event.getEntity();` then check `event.isEndConquered()`.

**Commit:** `a862278`

---

### Phase 1.14 — World + items

**Implemented:** All 5 picks (36, 37, 39, 40, 41).

**Tricky bits:**
- **#36 (50% XP keep on death):** Vanilla resets totalExperience on
  respawn, so simply adding the cleared XP back doesn't work. Used
  `PlayerEvent.Clone` with `wasDeath=true`, `event.getOriginal()` (the
  dead instance still carrying the pre-death XP), `reviveCaps()` to
  re-enable cap reads, copy 50% to new player via `giveExperiencePoints`,
  `invalidateCaps()` in finally.
- **#37 (XP orbs 30 sec):** Reflect into the private `age` field of
  `ExperienceOrb` and set it to `6000 - 600 = 5400` so it has only 600
  ticks (30 sec) left before vanilla's 6000-tick max age expires.

**Commit:** `1253d1a`

---

### Final delivery message for Phase 1.10–1.14

I sent the user a single .jar (`glebthanwolves-phase-1.10-1.14.jar`) with
a summary of all 25 mechanics grouped by phase, plus testing notes.

---

### User's reaction to the .jar — feedback

The user replied:

> "Сделать замедление если есть 10 стаков в инвентаре, надо учитывать что
> игрок может разделить стак."
>
> "Я не просил делать «Эндермены 16-блок агро»."
>
> "Я не просил «Рейн-фог»."
>
> "Жара в пустыне не работает."
>
> "Разве это всё что я просил?"
>
> "Не забудь что ты можешь смотреть уже готовые моды и код от людей в
> интернете, это будет даже лучше."

**My response:** Apologized, audited my implementation against the user's
25 picks, found three issues:

1. **Encumbrance threshold of 27** vs user wanting 10 stacks. The
   "split stack" concern is already handled (counting non-empty slots
   counts split stacks separately), but the threshold needed adjusting.
2. **Endermen aggro** — wrong number, was #8 not #10 picked. Removed.
3. **Witches everywhere (#10)** — missing entirely. Added.
4. **Rain fog (#21)** — user retracted this pick. Removed.
5. **Desert heat doesn't work** — investigated:
   - Damage was 1 HP / 15 sec, slow regen (potentially active when food >= 18)
     could heal it back faster than dealt. Bumped to 2 HP / 8 sec.
   - Damage source was `generic()` — no HUD icon. Switched to `inFire()`.
   - Strict `Biomes.DESERT` check was potentially missing badlands /
     savannas. Switched to temperature-based: `getBaseTemperature() >= 1.5F`.
   - Added `canSeeSky(pos)` so being under a roof shields you (sensible).
6. **Cold damage** got matching upgrade: 2 HP / 30 sec, `freeze()` source,
   `canSeeSky` requirement.

For "look at existing mods", I checked Tough as Nails on GitHub for their
temperature damage approach — they use a 5-tier scale (Icy/Cold/Neutral/
Warm/Hot) with multiple modifiers (biome, time, weather, altitude,
immersion, blocks, armor, items, season). Way more complex than what we
need. We stuck with our simpler "if X biome and Y conditions, deal Z damage
periodically" model.

**Commit:** `1aeff4c` — `Fix Phase 1.10-1.13: encumbrance threshold 10,
remove enderman+rain-fog, add witches everywhere, fix desert/cold damage`

---

### Final session message — handoff request

**User's final request:**

> "Сейчас я планирую продолжить работу на другом аккаунте и через другую
> нейросеть, смогу ли я перейти на другой аккаунт, указать этот проект на
> Гитхабе и промпт чтобы продолжить работу?"

(I plan to continue work on another account and through another AI, can
I switch to another account, point it to this GitHub project and a prompt
to continue work?)

**My answer:** Yes. Provided the user with:
- Repo URL, branch, PR link.
- Tech stack details (MC 1.20.1, Forge 47.2.0, Java 17).
- A starter prompt for the new AI.
- Caveats and gotchas the new AI might not know.

**User then asked:**
> "Создай все эти файлы, я хочу чтобы ты изложил туда ВСЮ РАБОТУ ДЕТАЛЬНО,
> все мои просьбы и то как мы общались, для полного понимания."

(Create all those files, I want you to lay out ALL the work in detail,
all my requests and how we communicated, for full understanding.)

**Resulting files (this commit):**
- `CHANGELOG.md` — every phase, every mechanic, every parameter.
- `ARCHITECTURE.md` — file structure, event flow, patterns, gotchas.
- `ROADMAP.md` — Phase 2.0+ design, principles, status of all 44 brainstorm
  items.
- `HISTORY.md` — this file.

---

### Phase 1.15 cave danger experiment, then removal

**User asked:**
> "думаешь стоит ещё что-то добавить в обычный мир?"

I suggested several possible mini-packs and the user selected "Пещеры опаснее".
I implemented a small experiment: deep unlit caves applied Darkness/Weakness
and rare cave ambushes could spawn nearby.

**User feedback:**
> "не уверен что работает, но и ладно, не особо нужна такая механика..."

Decision: remove the cave-danger experiment instead of tuning it further.

### Phase 2.0 — Armor gate

**User request:**
> "давай лучше будем думать как начать делать свои предметы и крафты чтобы всё
> шло по моей задумке. думаю надо начать с того что ты запретишь крафты брони,
> кроме кожаной и кольчуги, а если вдруг игрок найдёт другую броню или выбьет
> её, то её нельзя будет надеть и через инвентарь и взяв в руки нажав правую
> кнопку мыши"

**Implemented:**
1. Removed the Phase 1.15 cave mechanics from `HardcoreEvents.java` and
   `WorldEvents.java`.
2. Added datapack overrides under `data/minecraft/recipes/` to disable
   iron/gold/diamond armor crafting recipes with `forge:false`.
3. Added an armor gate in `HardcoreEvents.java`:
   - leather and chainmail armor are allowed;
   - iron/gold/diamond/netherite/turtle and other non-leather/non-chainmail
     armor are forbidden;
   - right-click equip is cancelled;
   - if forbidden armor reaches an armor slot by inventory/equipment change,
     it is removed and returned to inventory or dropped if inventory is full.

**User preference added during this phase:** after every mod change, send the
built jar and a short Russian checklist explaining how to test the new mechanic.

### Phase 1.15 dark night — added then reverted

**User asked** for a dark, hostile every-fifth-night mechanic. Implemented
with custom damage source, no-sleep block, fog/lightmap. After playtest the
user reverted via two `git revert` commits. Dark night is fully out as of
HEAD `7453869`.

### Phase 2.1 — Tool craft gate

**User request:**
> "давай запретим крафт всех инструментов(деревянных, железных и т.д) чтобы я
> мог выстроить свою цепочку развития, разумеется крафт инструментов которые
> добавляет мод запрещать не нужно. ещё есть ли у тебя идеи с тем что игрок
> может найти инструменты в мире, сможешь ли ты потом реализовать идею того
> что обычные инструменты можно только найти, но они будут слабее и не такие
> прочные?"

**User clarification on edge cases (after asking which utility items to gate):**
> "запретить Bow, Crossbow, Flint and steel(придумаем свой способ получение
> огня, например через трение, чуть позже это обдумаем), Shield(пока запрет,
> потом придумаем новый крафт и ослабим)."

**Implemented:**
1. Generated 30 datapack recipe overrides for tool crafts:
   - 25 crafting recipes: 5 tiers (wooden / stone / iron / golden / diamond)
     × 5 tool types (pickaxe / axe / shovel / hoe / sword).
   - 5 smithing recipes for netherite tools.
2. Generated 4 datapack recipe overrides for special items: bow, crossbow,
   flint and steel, shield.
3. All 34 files use `forge:conditions: [{type: forge:false}]` — the recipe
   never loads, so the player cannot craft these items.
4. Mod-added recipes (`glebthanwolves:*`) are unaffected because we only
   override `minecraft:` recipe IDs.

**Not implemented this phase (deferred to Phase 2.3):** the user's idea
about found vanilla tools being weaker than mod-crafted equivalents. A
design proposal is documented in `ROADMAP.md` → Phase 2.3 and is awaiting
user approval before any code is written.

### Phase 2.2 — Crude stone tools

**Origin:** the user built a prototype locally and shipped a jar
(`glebthanwolves-phase2.2-stone-tools.jar`) for review. The jar contained
5 new items, 5 recipes, 5 models, lang updates, and a new `GtwTiers` class.
Devin extracted the jar, decompiled the changed classes, diffed against
the PR #2 branch tip, and reconstructed the source from bytecode (mappings
were known: `m_6609_=getUses`, `m_6624_=getSpeed`, `m_6631_=getAttackDamageBonus`,
`m_6604_=getLevel`, `m_6601_=getEnchantmentValue`, `m_6282_=getRepairIngredient`).
No external code was used — `GtwTiers.java` is the user's own logic, just
expressed in source form rather than compiled form.

**User intent (paraphrased):**
> "сделаны свои каменные инструменты (они пока очень сыро сделаны)"
> ("own stone tools have been made — still very raw for now")

**Implemented:**
1. `items/GtwTiers.java` — `GTW_STONE` tier: durability 70, mining speed
   3.0, +1.0 attack damage, mining level 1, enchantability 5, repair =
   cobblestone. Same as vanilla `Tiers.STONE` minus durability and speed.
2. `ModItems.java` — 5 RegistryObjects using vanilla `PickaxeItem`,
   `AxeItem`, `SwordItem`, `ShovelItem`, `HoeItem` with `GTW_STONE`.
   Constructor args (damage, attack speed) match `Items.STONE_*` exactly.
3. `ModCreativeTabs.java` — adds 5 stone tools to the main GTW tab.
4. `assets/glebthanwolves/lang/en_us.json` + `ru_ru.json` — 5 new strings
   per language (`Crude Stone *` / `Грубый каменный *`).
5. `assets/glebthanwolves/models/item/stone_*.json` — 5 item models that
   reuse vanilla `minecraft:item/stone_*` textures, so no new PNGs are
   shipped.
6. `data/glebthanwolves/recipes/stone_*.json` — 5 shaped recipes on the
   3×3 crafting table. All use `plant_cordage` (gating the new tier
   through the bushcraft chain). `stone_sword` is unique — it has no
   stick, only cordage as the handle.

**Open issues carried into Phase 2.3+:**
- **First-cobblestone problem.** No vanilla pickaxe is craftable
  (Phase 2.1) and `stone_pickaxe` itself needs cobblestone. The player
  must currently find a vanilla pickaxe in chest / mob loot. Resolution
  options listed in `ROADMAP.md` → Phase 2.2.
- **No recipe advancements** were emitted, so the new recipes will not
  auto-unlock in the player's recipe book until a `RecipeProvider` runs.
- **Visual identity.** Models point at vanilla textures; new GTW stone
  tools are visually indistinguishable from vanilla in inventory.

### Phase 2.2.x — Crude wooden + iron tools

User asked to finish the GTW tool set "from wood through iron" before
resuming progression work:
> "давай закончим с модовыми инструментами, хочу чтобы уже были готовы
> деревянные, каменные и железные инструменты, модельки можешь взять
> как у ванильных инструментов(потом изменим)"

Translation: finish the mod tools, want wooden / stone / iron ready,
models can reuse vanilla textures (we'll replace later).

The PR mirrors the Phase 2.2 stone pattern: same "Crude" naming, same
~50% durability vs vanilla, ~75% mining speed vs vanilla, identical
HUD attack damage and attack speed (vanilla baselines). Two new tiers
in `GtwTiers.java` (`GTW_WOODEN`, `GTW_IRON`) plus 10 new tools in
`ModItems.java`. Recipes follow the same shape as Phase 2.2 stone with
material swapped (planks tag for wood, iron_ingot for iron); all require
`plant_cordage` to gate the entire tier set behind the bushcraft chain.

The existing `primitive_axe` (Phase 1.1) is kept as a separate starter
item — it's flint-based and represents a "found in survival" kind of
tool, distinct from the workbench-crafted `wooden_axe`.

### Phase 2.3a — Mining gate (default-deny scaffold, EMPTY whitelist)

**User pivot away from "found tools weaker":** the Phase 2.3 plan in
ROADMAP was originally to mark vanilla tools weaker via NBT. The user
proposed something more radical instead:
> "просто я хочу реализовать то что с блоков падает их кусочки а не сами
> блоки, чтобы блоки стали ценными, и не все блоки можно было скрафтить,
> но запрещать каждый блок для каждого инструмента слишком долго, я
> подумал что можно запретить всё для всех инструментов и руки, а потом
> разрешать по малому для каждого тира"

Translation: blocks should drop *shards* (not themselves) so that blocks
become a valuable resource. Banning every block per tool is too tedious;
instead, ban EVERYTHING by default and unlock blocks per tier in small
batches.

This splits the original Phase 2.3 into three sub-phases:
- **2.3a** — mining gate scaffold (this entry).
- **2.3b** — block-shard system (universal `stone_shard`, variant A).
- **2.3c** — found-tools-weaker (deferred; replaced by structure-loot
  replacement in Phase 2.7).

**Q&A in chat (paraphrased):**
- Q1: which blocks per tier? → "пока полный вайтлист" misread by Devin
  as "use my draft list"; user clarified "ПОЛНЫЙ, всё блокируй" — i.e.
  whitelist contents = empty. Block everything.
- Q2: how is tier determined? → use `Tier.getLevel()` of the held tool.
  Vanilla `correctToolForDrops` still controls drops (unchanged).
- Q3: shards universal or per-block? → variant A (universal stone_shard).
  Per-block shards may be added later only for ones that need identity.
- Q4: shard economy? → "крутая потеря" — block drops 2 shards, recipe is
  4 shards → 1 block (50% loss).
- Q5: which blocks first? → empty whitelist. Decide per-block as we go.
- Q6: silk-touch? → bypasses shard mechanic, gives whole block.
- Q7: found vanilla tools? → defer; later replace structure-loot spawns
  with GTW tools instead.

**Implemented in this PR:**
1. `events/MiningGate.java` — `PlayerEvent.BreakSpeed` returns 0 if the
   held tool's tier (hand=0, primitive/wood=1, stone=2, iron=3, diamond=4,
   netherite=5) is too low for the block's `breakable_by/<tier>` tag.
   `BlockEvent.BreakEvent` cancels as a defense-in-depth backup.
2. Six tag files (`hand` / `primitive` / `stone` / `iron` / `diamond` /
   `netherite`), all empty except for chained tag-includes (each tier
   transitively includes the lower one). So adding a block to e.g.
   `stone.json` automatically unlocks it for stone, iron, diamond, and
   netherite tiers.
3. `GlebThanWolves.java` registers `new MiningGate()`.

**Effect on gameplay:** spawn is now a hard soft-lock — nothing breaks.
This is intentional. The next PR (or several PRs) will unlock blocks per
tier as we design out from the spawn experience.

### Phase 2.2.y — Crude diamond tools

User asked to finish the diamond tier before starting the much larger
shard / 24-category mining-logic PR:
> "сделай уже алмазные инструменты, после того как ты сделаешь алмазные
> инструменты начинай работу над кусочками блоков и всеми этими
> семействами, дропами предметов и т.д, то есть логику развития, это
> будет самое глобальное что ты делал, потрать на это сколько надо
> времени, максимально внимательно и подробно учитывая все проблемы"

Translation: do the diamond tools, then start the big shard +
block-family + drop-logic work. Take the time you need on the big one
and be thorough.

This PR (Phase 2.2.y) is the small one: same "Crude" pattern as the
wooden / stone / iron tools — `GTW_DIAMOND` mirrors `Tiers.DIAMOND`
(level 3, +3.0 dmg) but with 50% durability (780 vs 1561) and 75%
mining speed (6.0 vs 8.0). Repair = `diamond`. Five new tools, vanilla
baseline HUD attack stats, vanilla textures reused. Recipes use
`minecraft:diamond` + stick + cordage (sword: cordage, no stick).

Mining gate is still empty so the tools can be crafted but cannot break
any block until Phase 2.3b lands.

### Phase 2.3b — Block shards + 24-category mining logic (in design)

**User's final design pivot** (message in chat): replaces the earlier
"variant A — universal stone_shard" idea. The user wrote a full
24-category breakdown, each with its own shard / fragment item, its own
mining-tier requirement, and a list of vanilla blocks that fall under
it. There is also a placeholder for a future netherite tier (24th
category SPECIAL_UNBREAKABLE).

**Devin reviewed the design and surfaced these issues, which the user
then resolved (responses inlined):**

1. **Crops in ORGANIC_SOFT** would replace `wheat` / `carrots` /
   `potatoes` / `beetroot` / `sweet_berries` with `organic_fiber` and
   make the food chain impossible. → User: "ты прав с едой ошибка";
   pick variant (a) — crops are excluded from the shard system.
2. **Leaves in LEAF_FAMILY** would suppress vanilla sapling / apple /
   stick drops, breaking tree regen → soft-lock the world. → User:
   "ты прав, листья и органику лучше не трогать"; chosen approach is
   variant (в) — `leaf_fragment` drops *additively* alongside vanilla
   drops.
3. **Gravel in GRAVEL_FAMILY** would suppress vanilla flint and break
   the spawn `primitive_axe` recipe (flint + stick + cordage). → User
   left it to Devin; chosen approach is *additive* gravel_piece + flint
   so that the spawn loop still works.
4. **Suspicious sand / suspicious gravel.** Vanilla archaeology brushes
   them for pottery sherds. → User left it to Devin; suspicious blocks
   are excluded from the shard system entirely.
5. **Boats in WOOD_FAMILY.** Boats are entities, not blocks. → Will be
   removed from the WOOD_FAMILY list (entity, can't be mined).
6. **Furnace / chest / crafting_table.** Breaking them yields the
   family shard (`stone_fragment` / `wood_chip`), not the placed
   item. This is BTW-style: once you place a station, you cannot
   relocate it. User accepted "всё остальное на твой вкус, главное
   чтобы это не ломало мод и игру"; design kept as-is.
7. **24 distinct shards instead of universal stone_shard.** This is a
   reversal of the Q3 answer ("вариант A"). User has now committed to
   24 shards in the new design. Devin will go with that.
8. **Netherite tier.** User capped tools at diamond. The `breakable_by/
   netherite` tag exists in code (Phase 2.3a) but stays empty.
9. **`correctToolForDrops` mismatches.** Some blocks (ice, rails,
   redstone_ore, glass) have vanilla rules that would let the player
   break the block at the gate-permitted tier but vanilla would still
   refuse the drop. Devin's plan: GLM injects the shard regardless of
   `correctToolForDrops`, so the drop-tier logic is unified through the
   gate alone. (Silk-touch carve-out preserves the original block.)
10. **Massive coverage list.** ~750 vanilla 1.20.1 blocks, many not in
    the user's 24 lists (bookshelf, composter, beehive, bee_nest,
    pumpkin / melon, mushroom_block / stem, banners, shulker boxes,
    coral, sponge, hay_block / slime / honey / honeycomb_block, mob
    heads, sculk family, amethyst clusters / buds / budding, copper
    variants, slabs / stairs / walls of every stone family, etc.).
    These will be folded into the appropriate categories or marked
    SPECIAL_UNBREAKABLE in `MINING_DESIGN.md` before code lands.

**Ore-recombine recipes** (per user: ores also drop fragments, then
combine back into "полноценные куски"):

- 4 `ore_fragment` → 1 `coal` OR 1 `raw_copper` (player picks recipe).
- 4 `metal_fragment` → 1 `raw_iron` OR 4 `lapis_lazuli` OR 4 `redstone`.
- 4 `rare_fragment` → 1 `raw_gold` OR 1 `emerald` OR 1 `diamond` OR 1
  `quartz`.
- 4 `ancient_fragment` → 1 `ancient_debris` (block, not netherite).

**Implementation scope (planned for the PR):**

1. 24 new items in `ModItems.java`.
2. 24 GLMs (one per category) in `data/glebthanwolves/loot_modifiers/`.
3. 24 block tags (`glebthanwolves:has_<name>_shard`).
4. Mining-gate whitelists (`breakable_by/<tier>`) updated to reference
   the 24 category tags via `forge:replace=false` includes.
5. Recombine recipes (one per re-craftable target — many).
6. `MINING_DESIGN.md` design doc with the full ~750-block annotated
   list, before code lands.

**Important security policy reminder added in this session:**
> "если ты взял что-то сторонние то должен отчитаться"
> "только код который ты вставляешь или мод который ты находишь должен быть
> с безопасного источника чтобы не словить вирус на 100%"

Going forward, before any external code is added (snippet, dependency, or
inspiration that turns into a copy of an algorithm), an explicit report
will be posted to the user with: source URL, author, license, exact
content taken, why, and audit (stars / last commit / open issues). The
user must respond "ок" before the change is committed. The same report is
copied into `HISTORY.md` under "External code references" (a new section
to be added once any external code is actually used; currently empty).

---

### Phase 2.3b — Block shards IMPLEMENTATION (delivered, then ROLLED BACK)

**Branch:** `devin/1778192365-phase-2-3b-impl` ([PR #8](https://github.com/gghyrmrwf/gleb-than-wolves/pull/8))
**Design doc PR:** [PR #7](https://github.com/gghyrmrwf/gleb-than-wolves/pull/7)
**Status:** ⚠️ Both PRs ABANDONED, replaced by PR #9 (Phase 2.3 tier-tightening).

**What was built (final scope after user trimming):**

- 18 shard categories (down from initial 25): `dirt_chunk`, `sand_pile`,
  `snow_chunk`, `wood_chip`, `stone_fragment`, `deepstone_fragment`,
  `nether_fragment`, `iron_fragment`, `gold_fragment`, `emerald_fragment`,
  `diamond_fragment`, `quartz_fragment`, `ancient_fragment`,
  `brick_fragment`, `hard_brick_fragment`, `glass_shard`, `ceramic_piece`,
  `concrete_dust`, `compressed_metal_fragment`, `precious_fragment`,
  `machine_scrap`.
- 151 recombine recipes (4 shards → 1 block, 8 shards → 1 station).
- Populated `breakable_by/*` whitelist tags from Phase 2.3a.
- `BlockShardModifier` custom GLM codec.
- `ItemMappings` event handler for old saves
  (`plant_fiber → organic_fiber`, `wood_chunk → wood_chip`).
- `MINING_DESIGN.md` (~1200 lines) — full 24-category specification.

**Commits in chronological order:**
- `e0d6002` — Phase 2.3b (1/8): rename plant_fiber → organic_fiber, wood_chunk → wood_chip
- `7d4dce6` — Phase 2.3b (2/8): register 23 shard items + models + lang
- `ebc5928` — Phase 2.3b (3/8): BlockShardModifier custom GLM
- `3a72710` — Phase 2.3b (4/8): 25 shard-category block tags
- `75a4afa` — Phase 2.3b (5/8): 25 shard GLM JSONs + remove obsolete Phase 1.1 GLMs
- `baa61fb` — Phase 2.3b (6/8): populate mining-gate breakable_by tags
- `f905406` — Phase 2.3b (7/8): 156 recombine recipes
- `7899c97` — Phase 2.3b (revised): drop leaf/gravel/grass/cloth shards
- `33cbb9a` — Phase 2.3b: fix MissingMappingsEvent import (in net.minecraftforge.registries)
- `9d0b88b` — Phase 2.3b: register MissingMappingsEvent on FORGE bus

**Bugs encountered during build/load:**

1. **`MissingMappingsEvent` import path** — I imported from
   `net.minecraftforge.event.MissingMappingsEvent` but in Forge 1.20.1 it
   moved to `net.minecraftforge.registries.MissingMappingsEvent`. Fixed in
   `33cbb9a`.

2. **`MissingMappingsEvent` event bus** — I registered the handler on the
   MOD event bus (`@Mod.EventBusSubscriber(bus = Bus.MOD)`) but the event
   fires on the FORGE event bus. Crash:
   `IllegalArgumentException: Method ... has @SubscribeEvent annotation,
   but takes an argument that is not a subtype of the base type interface
   net.minecraftforge.fml.event.IModBusEvent`. Fixed in `9d0b88b` by
   switching to `Bus.FORGE`.

**Catastrophic gameplay bugs (why this got rolled back):**

User loaded the jar after the bus fix and reported (verbatim):

> «полнейший бред и ужас, блоки от слова совсем не добываются, вообще ничем.
> кусок снега делает кусок льда, не понятный спрессованный метал делает блок
> рудного золота(вообще то это за "спрессованный метал" и зачем нужно рудное
> золото если есть просто золото, драгоценный осколок делает блок редстоуна
> (что вообще за бред и что это за осколок), металлолом делает вообще наблюдателя
> (что такое металлолом и почему он делает наблюдателя), почему из горсти песка
> получается красный песок, из комка земли вообще каменистая земля получается,
> что за древесная щепка из которой получается не нужный проигрыватель
> (вообще бред полный), каменный осколок делает андезит, твёрдый кирпичный
> обломок делает вообще светокамень, а кирпичный обломок делает саманные кирпичи.
> вообще что ты имеем, ты сделал полный мусор без логики, напутал крафты,
> сделал не нужные рецепты, вырезал 80% майнкрафт и даже не смог сделать
> механику добычи блоков (на всех блоках походу полный вайт лист),
> максимальный бред и халатность при работе с модом, в такое не то что
> невозможно играть, на это даже бредово смотреть»

**Three root causes of the disaster:**

1. **One-shard-to-many-recipes ambiguity.** Minecraft shaped/shapeless
   recipes don't support a "choose your output". Given 4 `stone_fragment`
   in 2×2, the game picks the **first matching recipe alphabetically**
   (`from_stone_fragment_andesite.json` → outputs andesite always). The
   other 13 stone_fragment recipes were dead. Same for snow_chunk → ice,
   sand_pile → red_sand, dirt_chunk → coarse_dirt, hard_brick_fragment →
   glowstone, brick_fragment → mud_bricks, wood_chip → jukebox.

2. **Overgeneralized shard categories.** "compressed_metal_fragment"
   bundled rails + hoppers + iron blocks + gold blocks → output `raw_gold`
   ore, which has no thematic sense. "precious_fragment" bundled
   diamond/emerald/amethyst blocks → output redstone block. These were a
   bad design from the start.

3. **Default-deny whitelist.** `MiningGate.java` set destroy speed = 0
   for blocks not in any `breakable_by/*` tag. Even with logic correct,
   the player experiences "blocks won't mine at all" because so many
   vanilla blocks were unintentionally outside the explicit lists.

**User's directive:** ROLLBACK to Phase 2.2.x state (PR #5), no shards,
slight tier-tightening only.

---

### Phase 2.3 — Tier tightening (replaces Phase 2.3a + 2.3b, current state)

**Branch:** `devin/1778243030-phase-2-3-tier-tighten`
**PR:** [#9](https://github.com/gghyrmrwf/gleb-than-wolves/pull/9)
**Status:** ✅ delivered, awaiting user testing.

**Approach:**

1. Removed `MiningGate.java` whitelist gate entirely.
2. Removed all 5 `breakable_by/*.json` tag files.
3. Cherry-picked diamond tools commit `71bc65a` from PR #6 → became
   `d61ac87` on this branch.
4. Added datapack overrides on vanilla `minecraft:needs_*_tool` tags
   (`src/main/resources/data/minecraft/tags/blocks/`):

**`needs_iron_tool` additions (require iron pickaxe to drop):**
- `copper_ore`, `deepslate_copper_ore` (was stone)
- `lapis_ore`, `deepslate_lapis_ore` (was stone)
- `nether_gold_ore` (was wood)
- `amethyst_block`, `budding_amethyst`, `amethyst_cluster`,
  `large_amethyst_bud`, `medium_amethyst_bud`, `small_amethyst_bud` (was wood)
- `end_stone`, `end_stone_bricks` + decorative variants (was wood)
- `bell` (was wood)
- `anvil`, `chipped_anvil`, `damaged_anvil` (was wood)
- `shulker_box` + 16 dyed colors (was wood)

**`needs_stone_tool` additions (require stone pickaxe to drop):**
- `nether_quartz_ore` (was wood)
- `magma_block` (was wood)

**Glowstone NOT tightened** — vanilla `glowstone` block does not have
`requiresCorrectToolForDrops`, so adding to `needs_iron_tool` has no
effect (drops dust regardless of tool). Would require a mixin or block
override to fix; left as-is.

**Why no `TierSortingRegistry.registerTier` call:**
GTW custom tiers (`GTW_WOODEN/STONE/IRON/DIAMOND`) are NOT registered in
Forge's `TierSortingRegistry`. Forge falls back to `tier.getLevel()`
integer comparison (0/1/2/3), which works correctly for our hierarchy.
If issues surface, registering them is a one-liner in `GtwTiers.java`.

**Commit:** `0290c57` — Phase 2.3: replace whitelist mining-gate with vanilla tier-tag tightening

**Replaces PRs:** #6 (diamond tools merged into this), #7 (design doc abandoned), #8 (shard impl abandoned).

**User's design philosophy going forward (verbatim):**

> «буду делать осколки только важным предметам и по очереди»
> (I will do shards only for important items, one by one)

> «просто смотри что ломает мод и тогда не делай этого»
> (Just check what breaks the mod and don't do that)

> «осколки могут быть из руды, твёрдых блоков, земли и т.д»
> (shards can be from ore, hard blocks, earth, etc — i.e. logically hard
> materials only, no wool/cloth/leaves/grass)

---

### Phase 2.5 — Cobblestone fragment (first targeted shard)

**Date:** 2026-05-15
**Branch:** `devin/1778243030-phase-2-3-tier-tighten` (continued)
**Status:** ✅ delivered, awaiting user testing.

**User decisions leading to this phase (verbatim):**

> «не хочу вообще делать незеритовые вещи. добавь осколок угля, но уголь
> можно сделать из двух осколков. убери осколки изумруда, лазурита.
> начинай делать»

> «ещё нужен осколок булыжника, который выпадает из всех камней с которых
> можно выбить булыжник в ванильном майнкрафте»

> «да, только правда стоит сделать 2 осколка булыжника для крафта»

**Final agreed shard list (planned, one PR each):**

| Order | Shard | Ratio | Source blocks |
|---|---|---|---|
| **1** | **cobblestone_fragment** | **2 → 1 cobblestone** | minecraft:stone (only vanilla "stone-that-drops-cobble") |
| 2 | iron_fragment | 4 → 1 raw_iron | iron_ore, deepslate_iron_ore |
| 3 | gold_fragment | 4 → 1 raw_gold | gold_ore, deepslate_gold_ore, nether_gold_ore |
| 4 | diamond_fragment | 4 → 1 diamond | diamond_ore, deepslate_diamond_ore |
| 5 | copper_fragment | 4 → 1 raw_copper | copper_ore, deepslate_copper_ore |
| 6 | quartz_fragment | 4 → 1 nether_quartz | nether_quartz_ore |
| 7 | coal_fragment | 2 → 1 coal | coal_ore, deepslate_coal_ore |

**Skipped intentionally:** netherite tools (user explicitly declined),
lapis_fragment, emerald_fragment.

**What got implemented in this PR:**

- Added `glebthanwolves:cobblestone_fragment` item.
- Added shapeless recipe `2× fragment → 1 cobblestone` (works in player
  inventory 2×2 grid, no crafting table needed).
- Overrode `minecraft:stone` loot table — non-silk-touch drop swapped from
  `minecraft:cobblestone` to `glebthanwolves:cobblestone_fragment`.
  Silk touch path unchanged (still drops `minecraft:stone` block).
- en/ru localization, item model reusing `minecraft:block/cobblestone`
  texture (placeholder, no custom asset yet).
- Added to main creative tab between `wood_chunk` and `primitive_axe`.

**Not implemented (intentional):**

- Chest loot containing cobblestone is untouched (user said leave chests
  alone for now).
- Andesite/granite/diorite/tuff/calcite/basalt unchanged — they drop
  themselves in vanilla, not cobblestone.
- Deepslate path unchanged — drops `cobbled_deepslate` (different item).

**Build:** `./gradlew build` clean. Output `glebthanwolves-1.0.0.jar`
79.5 KB. Delivered to user as `glebthanwolves-phase2.5-cobble-fragment.jar`.

**Next shard (after user OK on cobble):** iron_fragment.

**User confirmation:** "работает, можешь делать дальше" — green-lit
iron_fragment.

---

### Phase 2.6 — Iron fragment (targeted shard #2)

**Date:** 2026-05-15
**Branch:** `devin/1778243030-phase-2-3-tier-tighten` (continued)
**Status:** ✅ delivered, awaiting user testing.

**Approach:** copy-paste the Phase 2.5 pattern with two source blocks
(iron_ore + deepslate_iron_ore) and the canonical 4:1 fragment ratio.
Recipe is shaped 2×2 instead of shapeless 2× because it visually
distinguishes "this is a full ore worth of fragments" from "this is
just two leftover bits".

**What got implemented in this PR:**

- Added `glebthanwolves:iron_fragment` item (registered after
  COBBLESTONE_FRAGMENT in ModItems.java).
- Added shaped 2×2 recipe `4× fragment → 1 raw_iron` (works in player
  inventory 2×2 grid, no crafting table needed).
- Overrode `minecraft:iron_ore` loot table — non-silk-touch drop swapped
  from `minecraft:raw_iron` to `glebthanwolves:iron_fragment`. Same Fortune
  scaling (`apply_bonus(fortune, ore_drops)`) and `explosion_decay`
  functions preserved.
- Overrode `minecraft:deepslate_iron_ore` loot table — same swap.
- Silk touch path unchanged on both loot tables (drops the ore block).
- en/ru localization, item model reusing `minecraft:item/raw_iron` texture
  (placeholder, no custom asset yet — visually a raw iron lump).
- Added to main creative tab between `cobblestone_fragment` and
  `primitive_axe`.

**Why shaped 2×2 instead of shapeless:** with 4 fragments, shaped fills
the entire 2×2 grid and is unambiguous. Shapeless would also work but
shaped is slightly more "ceremonial" for the larger ratio — and players
seeing "must fill all 4 slots" clearly understand the cost.

**Why iron_fragment also gets Fortune:** vanilla iron_ore drops 1-4
raw_iron with Fortune III. If our fragment did NOT have Fortune scaling,
then Fortune III would give 1 fragment per ore = 0.25 ingots per ore,
making Fortune useless on the shard system. By preserving
`apply_bonus(fortune, ore_drops)` on the fragment, Fortune III yields up
to 4 fragments per ore = 1 ingot per ore on average — matching vanilla
non-Fortune behavior. So at max Fortune, the shard system effectively
disappears, which is acceptable as a high-investment endgame reward.

**Not implemented (intentional):**

- Chest loot containing raw_iron, iron_ingot, iron_nuggets is untouched
  (user said leave chests alone for now).
- Iron golem death drop unchanged (1-2 iron_ingot drop).
- Zombie/skeleton iron equipment drops unchanged.
- Iron block (`minecraft:iron_block`) breaks back to 1 iron_block as
  before (it's a placed item, not an ore).

**Build:** `./gradlew build` clean. Output `glebthanwolves-1.0.0.jar`
79.7 KB. Delivered to user as `glebthanwolves-phase2.6-iron-fragment.jar`.

**Next shard (after user OK on iron):** gold_fragment.

**User confirmation:** "работает, думаю дальше буду без тестов, просто
постепенно делай, отчитывайся, а я буду говорить тебе делать дальше или
нет. можешь продолжить" — user opted to skip per-shard testing for the
remaining 5 shards (gold, diamond, copper, quartz, coal). Agent
delivered all 5 in one batch under Phase 2.7-2.11.

---

### Phase 2.7-2.11 — Remaining targeted shards (one batch)

**Date:** 2026-05-15
**Branch:** `devin/1778243030-phase-2-3-tier-tighten` (continued)
**Status:** ✅ delivered, awaiting user testing.

All five remaining shards (gold, diamond, copper, quartz, coal) delivered
in a single commit and a single jar at the user's explicit request. Each
follows the Phase 2.5/2.6 loot-table-override + recipe pattern. Notable
asymmetries handled:

1. **nether_gold_ore** — vanilla drops 2-6 gold_nuggets, NOT raw_gold.
   Now drops 2-6 gold_fragment uniform, preserving "nether gold is richer
   than overworld" feel. Fortune uses `uniform_bonus_count` formula like
   vanilla nuggets, not `ore_drops`.
2. **copper_ore / deepslate_copper_ore** — vanilla drops 2-5 raw_copper.
   Now drops 2-5 copper_fragment uniform with Fortune(ore_drops),
   so net ingot cost is roughly 4× vanilla (2-5 fragments → 0.5-1.25
   raw_copper).
3. **coal uses 2:1 ratio** — different from all others (4:1) per explicit
   user spec. Shapeless recipe (2 items in inventory grid) instead of
   shaped 2×2.

**Final shard table for the mod:**

| # | Shard | Ratio | Recipe | Source ores | Fragment count per ore |
|---|---|---|---|---|---|
| 1 | `cobblestone_fragment` | 2:1 cobblestone | shapeless | minecraft:stone | 1 |
| 2 | `iron_fragment` | 4:1 raw_iron | shaped 2×2 | iron_ore, deepslate_iron_ore | 1 (Fortune ore_drops) |
| 3 | `gold_fragment` | 4:1 raw_gold | shaped 2×2 | gold_ore, deepslate_gold_ore | 1 (Fortune ore_drops) |
|   |  |  |  | nether_gold_ore | 2-6 (Fortune uniform_bonus_count) |
| 4 | `diamond_fragment` | 4:1 diamond | shaped 2×2 | diamond_ore, deepslate_diamond_ore | 1 (Fortune ore_drops) |
| 5 | `copper_fragment` | 4:1 raw_copper | shaped 2×2 | copper_ore, deepslate_copper_ore | 2-5 (Fortune ore_drops) |
| 6 | `quartz_fragment` | 4:1 nether_quartz | shaped 2×2 | nether_quartz_ore | 1 (Fortune ore_drops) |
| 7 | `coal_fragment` | 2:1 coal | shapeless | coal_ore, deepslate_coal_ore | 1 (Fortune ore_drops) |

**Skipped intentionally:** netherite tools (user declined), lapis_fragment,
emerald_fragment.

**Build:** `./gradlew build` clean. Output `glebthanwolves-1.0.0.jar`
90.8 KB. Delivered to user as `glebthanwolves-phase2.7-2.11-all-shards.jar`.

**Future considerations:**
- Chest loot still bypasses the shard system (per user). If shards
  prove too slow, chest loot could be GLM-converted in a future pass.
- No custom textures yet. All fragment items reuse the closest vanilla
  texture (raw_iron, raw_gold, diamond, raw_copper, quartz, coal,
  cobblestone). Custom textures could be a polish PR.

---

### Phase 3.0 — Nether idle hazard

**Date:** 2026-05-15
**Branch:** `devin/1778243030-phase-2-3-tier-tighten` (continued)
**Status:** ✅ delivered, awaiting user testing.

**User-requested mechanic:** in the Nether, if the player stands on the
ground for 5 seconds without jumping, they catch fire. Jumping puts the
fire out. The user specifically called out the obvious exploit ("place
a block above your head and spam jump") and asked it be blocked — the
player must actually leave the ground.

**Implementation idea — track airborne ticks, not jump key presses.**
The simplest robust way to distinguish a real jump from a head-bump
exploit jump is to require the player to remain airborne for some
minimum number of consecutive ticks. Under a 2-tall ceiling (the only
practical "exploit cage" you can build in a couple of seconds), a jump
launches the player upward at velocity 0.42 blocks/tick. They contact
the ceiling almost immediately (within ~1 tick at vanilla player
height 1.8 + 0.2 headroom), and vertical velocity is zeroed, so they
land within 1–2 ticks. We picked 5 ticks (~250 ms) as the real-jump
threshold — well above the head-bump zone, well below the ~12-tick
duration of a free-air jump arc.

**Why we don't just listen to the jump key event.** Forge does have a
`LivingEvent.LivingJumpEvent` that fires when `Player#jumpFromGround`
is called. But this fires on the keypress side, including head-bump
attempts. To block the exploit we need the airborne-duration check
anyway, so the simpler design is to drive everything off
`PlayerTickEvent` + `player.onGround()` and skip the jump event
entirely.

**Why we don't use a Forge-managed capability or persistent NBT.** The
per-player state (idle counter, airborne streak, "our fire" flag) is
ephemeral — it lives only while the player is in the Nether and is
reset on dimension change, logout, and respawn. A static
`ConcurrentHashMap<UUID, Integer>` is the cheapest implementation. We
clean up entries on `PlayerLoggedOutEvent` to avoid unbounded growth.

**Fire-clearing safety.** The mechanic distinguishes "our fire" (set
by us via `setSecondsOnFire(1)`) from "other fire" (lava, fire block,
blaze attacks) via a per-player boolean flag. On a real jump we only
call `clearFire()` if the flag is set, AND vanilla logic restores any
non-self fire on the next tick anyway (lava contact reapplies
`setSecondsOnFire(15)` automatically). So a player standing in lava
who jumps will see only a 1-tick gap before lava-fire resumes — the
mechanic doesn't accidentally save them from lava.

**Edge cases reviewed:**
- Player riding a strider on lava → strider is `onGround()` because
  it walks on lava. Player inherits onGround from vehicle in most
  cases. The player will burn after 5s of standing still on a strider.
  This is intentional: standing still on a strider is exactly the
  kind of "idle in Nether" the mechanic targets.
- Player on a minecart in the Nether → similar: counts as standing if
  the minecart isn't moving vertically. We accept this.
- Player using elytra in the Nether → airborne, timer resets. Good.
- Player climbing scaffolding/vines → airborne, timer resets after 5
  ticks of climbing. Good (climbing is active state change).
- Player blown into the air by a ghast → airborne, timer resets. Fair.

**Future tuning ideas (NOT in this PR):**
- Damage scaling — currently the player just burns for 1s per tick
  past the threshold. If the burn feels too gentle, we could add a
  direct `player.hurt(...)` call for extra damage on top of the fire.
- Warning sound at 4s (just before ignition).
- Hardness scaling by Nether depth or biome.
- Same mechanic in the End for a different reward structure.

**Build:** `./gradlew build` clean. Output `glebthanwolves-1.0.0.jar`
~92 KB. Delivered to user as
`glebthanwolves-phase3.0-nether-idle-hazard.jar`.

**User testing result:** "доп урон не нужен, а так всё работает" —
mechanic accepted as-is. Extra damage layer is NOT desired.

---

### Phase 3.1 — Piglins always hostile

**Date:** 2026-05-15
**Branch:** `devin/1778243030-phase-2-3-tier-tighten` (continued)
**Status:** ✅ delivered, awaiting user testing.

**User-requested mechanic:** "Свинолюди всегда враждебны" — make piglins
unconditionally hostile to the player, regardless of whether the player
wears gold armor. Selected from a multiple-choice list of Nether
difficulty ideas.

**Design space exploration.** Vanilla piglin neutrality is enforced
through the piglin's behavior tree at two layers:

1. **Target acquisition layer.** The piglin's brain has a memory slot
   `NEAREST_TARGETABLE_PLAYER_NOT_WEARING_GOLD` (`MemoryModuleType<Player>`).
   Vanilla `PiglinAi.updateActivity` and related methods populate or
   clear this slot every tick based on a scan of nearby players and
   their `EquipmentSlot.HEAD/CHEST/LEGS/FEET` items. If any equipped
   item is in the `minecraft:gold_armor` tag (gold helmet, chest,
   leggings, or boots), the player is filtered out of this slot.
   Without a populated slot, the piglin never picks the player as a
   target.

2. **Admiration sub-state layer.** When a piglin picks up a `gold_*`
   item from the ground, vanilla sets the `ADMIRING_ITEM` brain
   memory slot to true and the `ADMIRING_DISABLED` cooldown is
   armed. While admiring, all attack tasks are paused for ~5 seconds.
   This is the basis for the "throw gold to distract" trick.

**Bypass strategy chosen.** We could have used a mixin to override
`PiglinAi.isWearingSafeArmor(Player)` directly. That's the "cleanest"
approach but introduces a mixin dependency for a single boolean check.
Instead, we drive everything from the Forge events we already use, by
directly manipulating the piglin's brain memory each half-second:

- Force-set `NEAREST_TARGETABLE_PLAYER_NOT_WEARING_GOLD` to the nearest
  eligible player. Vanilla will clear it next tick (if gold armor is
  worn), then we set it again at the next check interval. The piglin's
  attack-target propagation logic picks up the slot in its regular
  behavior tree, so animations, weapon raise, and chase logic all
  work normally.
- Erase `ADMIRING_ITEM` and `ADMIRING_DISABLED`. Any admire state the
  piglin entered in the last 10 ticks is cancelled.

**Why 10-tick interval and not every tick.** A 10-tick (0.5 s) cycle is
imperceptible to gameplay — the piglin will lose its target for at most
half a second before reacquiring. Vanilla AI itself runs attack-target
re-evaluation on a similar cadence. Per-tick force-set would be wasted
CPU.

**Detection radius preserved at 16 blocks.** Vanilla piglin detection
range varies with player armor/state, but the typical "no gold armor"
detection is ~16 blocks. We use 16 to match. Increasing this would buff
piglin detection range beyond vanilla, which the user did NOT request
and would be a separate dial.

**Side effect: piglin trading is broken.** Players can no longer trade
with piglins by throwing gold ingots, because admiration is bypassed.
The user's stated intent ("always hostile") aligns with this — piglins
are enemies, not vendors. If trading needs to be preserved later, it
would require a more surgical approach (e.g. only bypass admiration
when the player is the one being targeted, allow admiration for
ambient gold pickups).

**What we deliberately do NOT touch:**

- **Baby piglins** (`piglin.isBaby() == true`). In vanilla they never
  attack; they only panic-flee from zombified piglins / soul fire /
  etc. Force-targeting babies would create AI flicker between attack
  and flee tasks.
- **Piglin Brutes** (`net.minecraft.world.entity.monster.piglin.PiglinBrute`).
  Separate class, already always-hostile in vanilla.
- **Zoglins** (zombified piglins). Already always-hostile.
- **Invisibility respect.** Vanilla piglins ignore invisible players;
  we preserve that.

**Build:** `./gradlew build` clean. Output `glebthanwolves-1.0.0.jar`
~92 KB. Delivered to user as
`glebthanwolves-phase3.1-piglins-always-hostile.jar`.

---

### Phase 3.2 — Zombified piglins always hostile

**Date:** 2026-05-15
**Branch:** `devin/1778243030-phase-2-3-tier-tighten` (continued)
**Status:** ✅ delivered, awaiting user testing.

**User-requested mechanic:** after Phase 3.1 made piglins always
hostile, the user immediately asked for the same treatment for
"свинозомби" — Zombified Piglins. (Note: in Russian Minecraft
terminology, "свинозомби" refers specifically to the `ZombifiedPiglin`
entity — the neutral-when-unprovoked undead piglin spawning naturally
in the Nether — and NOT to Zoglins, which are called "зоглины" and are
already always-hostile in vanilla.)

**How vanilla handles zombified piglin neutrality.** Unlike piglins
(which use the modern brain/memory system), zombified piglins use the
older `NeutralMob` interface + goal-based AI. The interface defines:

- `setPersistentAngerTarget(UUID)` — store who you're mad at.
- `getPersistentAngerTarget()` — UUID of the current anger target.
- `setRemainingPersistentAngerTime(int)` — ticks left to stay angry.
- `getRemainingPersistentAngerTime()` — current anger countdown.
- `startPersistentAngerTimer()` — vanilla helper that picks 20–39 s
  random.

Vanilla provocation routes: `ZombifiedPiglin#hurt(...)` calls
`maybeAlertOthers(...)` which iterates nearby zombified piglins within
`ALERT_RANGE_Y/XZ` and calls `setPersistentAngerTarget` + a random
20–39 s timer. After the timer expires, the piglin returns to neutral.

**Our bypass: skip the provocation step entirely.** Every 10 ticks
(~0.5 s), for each adult zombified piglin we scan nearby players,
pick the closest eligible one, and:

```java
zp.setPersistentAngerTarget(nearest.getUUID());
zp.setRemainingPersistentAngerTime(ANGER_REFRESH_TICKS);  // 1200 = 60s
if (zp.getTarget() != nearest) zp.setTarget(nearest);
```

The 60 s anger refresh is much longer than the 0.5 s refresh cadence —
the timer never runs out, the piglin stays permanently angry, and the
herd-alert mechanic keeps amplifying the effect.

**Why not handle via `NeutralMob.maybeAlertOthers` directly.** That
method is protected and tied to `LivingEntity#hurt` flow. Force-calling
it would require a synthetic damage event. Setting the anger target +
timer directly is simpler and avoids any unwanted side effects of
fake damage (death loop, knockback, etc.).

**Symmetry with Phase 3.1.** Same per-tick scan structure, same
16-block trigger radius, same eligibility filters (creative /
spectator / dead / invisible all excluded), same baby exclusion. The
only difference is the brain-memory vs anger-target mechanism (piglin
brain vs zombified piglin NeutralMob).

**Combined effect with prior phases.** In the Nether, the player now
faces three independent threats:
1. Idle-fire after 5 s of standing (Phase 3.0).
2. Hostile piglins even with full gold armor (Phase 3.1).
3. Hostile zombified piglins from the moment they see you (Phase 3.2).

There is no longer a "safe" Nether biome by armor choice or by
crouching still. The player must move, must jump periodically, must
treat every piglin and zombified piglin as a credible threat.

**Build:** `./gradlew build` clean. Output `glebthanwolves-1.0.0.jar`
~92 KB. Delivered to user as
`glebthanwolves-phase3.2-zombified-piglins-always-hostile.jar`.

---

### Phase 3.3 + 3.4 — Ghast detection range, Magma block hazard

**Date:** 2026-05-15
**Branch:** `devin/1778243030-phase-2-3-tier-tighten` (continued)
**Status:** ✅ delivered, awaiting user testing.

**User-requested mechanics (both in one batch):** "сделай 'Гасты слышат
на 100 блоков' и 'Магма-блоки сильнее жгут'". Two independent Nether
difficulty tweaks bundled into one PR / build.

#### Phase 3.3 — Ghasts at 100 blocks

**Mechanism chosen.** Vanilla Ghast's `targetSelector` adds a single
`NearestAttackableTargetGoal<Player>(this, Player.class, 10, true,
false, (e) -> Math.abs(e.getY() - this.getY()) <= 4.0D)`. The `10`
is `randomInterval` (recalc every 10 ticks). The Y filter is the
predicate. The detection radius is NOT a parameter of the goal — it
comes from `Mob#getFollowDistance()` which is the `FOLLOW_RANGE`
attribute.

Vanilla Ghast base FOLLOW_RANGE = 64. We bump to 100 at spawn via
`EntityJoinLevelEvent`. No mixin needed, no goal manipulation.

**Why not buff via attribute modifier instead of base value.**
`setBaseValue` is symmetric with vanilla: a base value is what the
attribute would be if there were no modifiers. Any additive or
multiplicative modifier (e.g. from `HardcoreEvents`' +15% enemy HP
buff layer, or future difficulty mods) stacks on top correctly. An
ADDITION modifier would not commute with multiplicative modifiers and
could cause unexpected behavior in the presence of mod stacking.

**Why we don't lift the Y filter.** The ±4 block Y filter prevents
ghasts from sniping at players through entire Nether biomes when they
have no line of sight. Removing it would make ghasts target hidden
players from extreme distances, which would feel cheap and broken.
The user asked for "hear at 100 blocks", which we interpret as
horizontal detection within a reasonable vertical spread — the
vanilla Y filter captures that intent.

**Other ghast-related ideas NOT touched here:**
- Ghast HP buff — already covered by `HardcoreEvents`.
- Fireball speed / damage — separate concern, not requested.
- Ghast spawn rate — separate, not requested.
- Ghast ranged predict — vanilla ghasts shoot at last known position
  with some lead. Not touched.

#### Phase 3.4 — Magma block hazard

**Mechanism chosen.** Vanilla magma damage is in `MagmaBlock#stepOn`:

```java
public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
    if (!entity.isSteppingCarefully() &&
        entity instanceof LivingEntity &&
        !EnchantmentHelper.hasFrostWalker((LivingEntity)entity)) {
        entity.hurt(level.damageSources().hotFloor(), 1.0F);
    }
    super.stepOn(level, pos, state, entity);
}
```

`stepOn` is called when an entity moves into the block's footprint.
For a player standing still with micro-jitter, this is called every
tick, but vanilla entity invulnerability frames (`invulnerableTime`)
limit actual damage to roughly 1 per 20 ticks (1 second).

**Augmentation strategy.** Rather than overriding `MagmaBlock` (which
would require an AT or replacing the block registration), we add an
*orthogonal* per-tick layer in `LivingEvent.LivingTickEvent`:

1. Check the entity is standing on `Blocks.MAGMA_BLOCK` via
   `getBlockState(entity.getOnPos()).is(Blocks.MAGMA_BLOCK)`.
2. Apply the same vanilla exemption checks (crouching, frost walker,
   fire immune, airborne).
3. Add Slowness I + 2 s fire each tick.

The vanilla `stepOn` damage continues as normal — we don't replace
it, we add on top.

**Damage math.** Vanilla:
- Magma `stepOn` damage: ~1 / second (invuln-limited)
- Total: ~1 DPS

After Phase 3.4:
- Magma `stepOn` damage: unchanged, ~1 / second
- Fire damage: 1 / second (vanilla fire tick rate)
- Total: ~2 DPS

Plus Slowness I makes escape harder (15% slower walk speed). Net
feel: stepping on magma is now ~2× more punishing.

**Why `setSecondsOnFire(2)` and not direct damage.** Two reasons:
1. Fire is a vanilla mechanic the player already understands — they
   know to put it out, they hear the crackle sound, they see the
   overlay. Adding direct damage on top of vanilla magma would feel
   like a bug ("why am I taking damage so fast?").
2. Fire damage handling already correctly avoids invuln-frame
   stacking issues. Vanilla `LivingEntity.baseTick` ticks fire
   damage at its own cadence, separate from contact damage. Our
   bonus naturally interleaves with vanilla magma damage instead of
   colliding with invuln frames.

**Why not also affect player-placed magma in safe bases.** The user
didn't request a dimension/biome filter, and adding one would
require either:
- Tagging player-placed vs natural magma (NBT state — not present
  in vanilla magma blocks).
- A claim system (out of scope).

So magma is uniformly hazardous everywhere. If the user wants
furnace-style magma rooms in their base, they can place ladders or
slabs to avoid stepping on it.

**Build:** `./gradlew build` clean. The compiler emits one
deprecation warning for `setSecondsOnFire` (Forge 1.20.1 marks it
deprecated in favor of `setRemainingFireTicks(seconds * 20)`, but
both still work). Output `glebthanwolves-1.0.0.jar` ~95 KB.
Delivered to user as
`glebthanwolves-phase3.3-3.4-ghasts-and-magma.jar`.

---

## External code references

(Empty — no external code has been used yet. When external code is first
used, a row per source will be added here following the format described
in the Phase 2.1 entry above.)

---

## Original 44-mechanic brainstorm (verbatim text I sent the user)

For continuity, here's the full original brainstorm with all 44 items the
user picked from. The user's selection is annotated with `[picked]`.

### A. Movement and navigation
1. **★ Sneak even slower** — sneak speed ×0.5 (vs vanilla ×0.7). [picked]
2. **★ Snow slows** — standing in snow_layer / powder_snow → -25% speed. [picked]
3. **★ Ice obscenely slippery** — friction ×1.5, you slide for 2 sec after stopping. [picked]
4. **★★ Carry fatigue** — inventory > 27 stacks → Slowness I until you unload. [picked, with note "если хорошо реализуешь, можешь сделать чуть жестче"]
5. **★ Jump -10%** — JUMP_STRENGTH negative modifier.
6. **★ Swim -30%** — water horizontal speed ×0.7. [picked]
7. **★★ Ladders slower** — ladder/vine climb ×0.7. [picked]

### B. Hostile fauna and AI
8. **★ Endermen aggro at 16 blocks** — without visual contact.
9. **★ Quieter creepers** — 50% chance to detonate without a hiss. [picked]
10. **★ Witches everywhere** — not just in huts, every 30 min within 64 blocks. [picked, "но не сильно часто"]
11. **★ Drowned spawn in any water at night** — including rivers/lakes.
12. **★★ Husk in any biome** — 10% chance vs zombie. [picked]
13. **★ Ghasts throw 1.5× more often.** [picked]
14. **★★ Headless creeper** — 20% chance, ×1.5 explosion radius. [picked]

### C. Environment
15. **★ More frequent storm lightning** — ×3 chance to strike player on open ground. [picked]
16. **★★ Sandstorms in deserts** — 15-min interval, 2-min Blindness + Slowness.
17. **★★ Desert heat at noon** — -0.5 HP / 15 sec without helmet. [picked]
18. **★ Snowy biome cold** — -0.5 HP / 60 sec without chest armor. [picked]
19. **★★ Lava burns longer** — Fire III 10 sec instant, +1 first hit. [picked]
20. **★★ Meteors at night** — 1% chance per night to spawn meteor blocks. [picked]
21. **★★ Rain fog** — render distance reduced / Blindness pulses in rain. [picked, then retracted]

### D. Player physiology
22. **★ Bleeding** — 20% on sword/axe/arrow hit, Wither I 60 sec.
23. **★ Zombie infection** — 15% on zombie hit, Hunger II 5 min. [picked]
24. **★ Fever** — after death, Weakness I + Mining Fatigue I 5 min. [picked]
25. **★ Fall shock** — fall >5 blocks, Slowness II 3 sec.
26. **★★ Mining fatigue** — >100 blocks dug → Mining Fatigue I 30 sec.
27. **★★ Sleep mandatory** — 2 missed nights → Slowness I + Weakness I until you sleep. [picked]

### E. Perception / UX
28. **★ Darker darkness** — light 0 → micro Blindness flicker.
29. **★ Underground compass random** — y < 50, compass spins.
30. **★★ HUD hidden at low HP** — HP < 2 hearts → hide hotbar/exp/food bars.

### F. Equipment / items
31. **★ Tools 1.5× faster break** — durability ×0.7.
32. **★ Armor 1.5× faster break** — same.
33. **★ Dropped items take damage** — fall + collide → -1 durability per block.
34. **★ Items despawn faster** — 90 sec instead of 5 min.

### G. World rules
35. **★ Bed only sets spawn 70%** — 30% you respawn at world spawn.
36. **★ XP loss 50% on death** (vs vanilla 30% kept). [picked]
37. **★ XP orbs live 30 sec** — pick up fast or lose. [picked]
38. **★★ Bedrock layer pushed up** — y=0 → y=5, rarer diamonds.

### H. Biome (basic, before redesign)
39. **★★ Mud and swamps slow** — Slowness I in 1-block deep water. [picked]
40. **★ Cactus more painful** — 1 → 2 HP per tick contact. [picked]
41. **★ Sweet berries serious** — 0.5 → 1.5 HP per movement. [picked]

### I. Atmospheric
42. **★★ Cave sounds** — boost cave_ambient frequency.
43. **★★ Eyes in the dark** — particles in bushes at night, 50% real / 50% phantom.
44. **★ Mobs moan more often** — sound events every 30 sec when zombie/skeleton near.

---

## Style of testing the user expects

- Manual in-game tests with cheats. Common pattern:
  ```
  /gamerule keepInventory true
  /gamerule doImmediateRespawn true
  /gamemode survival
  /effect clear @s
  /time set noon  # or night, or day
  /weather thunder 600  # for lightning
  /tp ~ ~ ~ <coords>     # to specific biome
  /summon <entity> ~5 ~ ~5
  ```
- The user will test each .jar themselves and report findings via chat.
- The user values **clearly visible feedback** — invisible mechanics
  (small HP drips, rare events) are reported as "doesn't work" even when
  the code is correct. Always make damages visible enough to outpace
  vanilla regen + slow regen, use damage sources whose icons match the
  cause, and round numbers up rather than down.
- The user will accept the agent saying "I tested this in dev client and
  saw X" but doesn't expect the agent to do exhaustive in-game QA. Most
  validation is on the user's side.

---

## How the next AI should work

1. **Read these four .md files** (`CHANGELOG.md`, `ARCHITECTURE.md`,
   `ROADMAP.md`, `HISTORY.md`) before doing anything.
2. **Read `HardcoreEvents.java`** at least once. Most mechanics live there;
   the file's structure and conventions are the model for new code.
3. **Don't add a new file when you can extend an existing handler.** Adding
   another `@SubscribeEvent` method to an existing class is preferred over
   adding a whole new event-handler class.
4. **All numeric parameters as named constants at the top of the file.**
5. **Test compile (`./gradlew compileJava`) often** — it's fast (5 sec).
6. **Build full .jar (`./gradlew build`)** before sending the user anything.
7. **Match the user's communication style** — short, in Russian, focused
   on the next concrete deliverable. Cheap, friendly, not ceremonial.
8. **When confused, ask** — the user appreciates an upfront "did you mean
   X or Y?" over a wrong implementation.

When the user signals "ready to do redesign work" (Phase 2.0+), follow the
plan in `ROADMAP.md`. Don't deviate without explicit approval.
