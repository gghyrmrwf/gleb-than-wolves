# History

A faithful chronological record of how this mod was built, including every
user request (verbatim where possible, in Russian), the decisions taken, the
bugs encountered, and the fixes. Use this as context for any AI agent or
human collaborator picking up the project later.

The current author was a Devin AI session working with GitHub user
`gghyrmrwf`. The session began with an empty repository and reached
**Phase 2.1 dark night**, with a **post-1.14 fix pass** in response to user
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

### Phase 2.1 — Every 5th night is darker

**User request:**
> "сделай механику того что каждую 5 ночь темнее чем обычно, но не обычная
> слепота а и правда хорошо сделанная темнота в которой хоть чуть чуть но
> видно, из того что хорошо видно это луну, её должно быть видно отлично, и
> самое главное если ты будешь стоять в темноте слишком долго(5 минут) то тебя
> убьёт невидимая сущность(просто килл но с надписью в чате будто игрок умер
> от чего-то загадочного) также нужно учитывать что игрок до этого спал или
> нет, чтобы рассчитать правильно день в который будет тёмная ночь, в такую
> ночь нельзя спать"

**Implemented:**
1. Added `DarkNightEvents.java` for server-side schedule and player rules.
2. Dark nights are calculated from `getDayTime() / 24000 + 1`, so sleeping on
   earlier nights still advances the calendar and does not break the 5th-night
   rhythm.
3. Added `DarkNightClientEvents.java` for client fog/lightmap darkness without
   applying vanilla Blindness/Darkness effects.
4. Sleep is blocked on dark nights.
5. Standing in block light ≤ 1 without sky visibility for 5 minutes during a
   dark night kills the player with `glebthanwolves:the_darkness` and custom
   localized death messages.

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
