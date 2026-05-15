# Gleb Than Wolves — Полный handoff (для следующего ИИ или человека)

> **Цель файла:** один документ, в котором есть всё, что нужно знать,
> чтобы продолжить работу с этим модом. Включает историю, текущее
> состояние, все ссылки, все провалы и причины откатов, и план дальше.

---

## 0. Базовые ссылки

| Ресурс | Ссылка |
|---|---|
| Репозиторий | https://github.com/gghyrmrwf/gleb-than-wolves |
| Владелец репо | `gghyrmrwf` (он же тестировщик и заказчик) |
| Mod ID | `glebthanwolves` |
| Java-пакет | `com.gghyrmrwf.glebthanwolves` |
| Forge версия | 1.20.1 / 47.x (47.2.0+ MDK, 47.4.20 на клиенте пользователя) |
| Java | 17 |
| Текущая активная ветка | `devin/1778243030-phase-2-3-tier-tighten` |
| Актуальный PR | **#9** — https://github.com/gghyrmrwf/gleb-than-wolves/pull/9 |
| Актуальный коммит | `0290c57` |
| Версия мода в `mods.toml` | `1.0.0` |

---

## 1. Что такое мод (общая концепция)

**Gleb Than Wolves** — это «Better-Than-Wolves-style» хардкорный
оверхол ванильного выживания в Minecraft 1.20.1 на Forge.

**Главные принципы:**

1. **Нельзя голыми руками рубить дерево.** Стартовая прогрессия:
   трава → волокно → жгут → кремень с гравия → примитивный топор.
2. **Прогрессия инструментов «дикий → каменный → железный → алмазный».**
   Все ванильные верстаковые рецепты топоров/мечей/кирок/лопат/мотыг
   запрещены — заменены модовыми «crude_*» инструментами.
3. **Мир враждебный.** Снижено HP игрока (5 сердец), быстрее голод,
   агрессивные железные големы, дикие волки, ускоренный кислород,
   медленные лодки, дождь царапает, ночные холода, лава дольше горит,
   фантомы спавнятся даже после сна, и т.д. (Phase 1.3-1.14)
4. **Минимум новых блоков и предметов.** Большинство механик —
   через события Forge поверх ванильных предметов. Новых только
   primitive_axe / wood_chunk / 5×crude_stone / 5×crude_wooden /
   5×crude_iron / 5×crude_diamond / plant_fiber / plant_cordage.
5. **Никаких осколков (shards) пока что.** Был провал (см. секцию 5),
   осколки добавляем по одному, по запросу пользователя, после
   стабилизации mining-gate.

---

## 2. Стиль общения с пользователем

- Пользователь пишет **по-русски**. Отвечать тоже по-русски.
- Стиль коротких директивных сообщений: «сделай X» → «протестил, баг Y» →
  «поправь и снова дай jar».
- Пользователь **сам тестит .jar в реальном клиенте** Minecraft. ИИ не
  имеет доступа к запуску игры — только к компиляции через `./gradlew build`.
- Пользователь ценит **прагматизм и скорость доставки jar** над «идеальным
  планом». Лучше «вот рабочий jar с оговорками» чем «вот идеальный план,
  кода нет».
- Пользователь **не хочет .txt-changelog для каждой фазы** (явно сказал).
  Только код + коммиты. Этот HANDOFF.md — исключение, по запросу.
- Пользователь **открыт к архитектурным советам**, но окончательные
  дизайн-решения принимает сам.
- Пользователь **жёстко критикует когда что-то не работает.** Использовал
  слова «полный мусор», «халатность» — это правомерная обратная связь, не
  атака. Слушай и переделывай.

---

## 3. Хронология (полный список всех фаз)

### Phase 0 — Каркас Forge MDK
- **Что:** скелет мода, регистрации, точка входа.
- **Коммит:** `113cb11`
- **PR:** [#1](https://github.com/gghyrmrwf/gleb-than-wolves/pull/1)
- **Ветка:** `devin/1777923068-phase-0-scaffold`

### Phase 1.1 — Примитивный бушкрафт
- **Что:** plant_fiber (15% с большой травы), plant_cordage, primitive_axe,
  запрет ломки брёвен голыми руками, дроп палок с листвы, +шанс
  саженцев снижен.
- **Коммит:** `c0b805a` (исходный) + `191ab9b` (фикс рецептов 2×2 без верстака).
- **Ветка:** `devin/1777925074-phase-1-1-bushcraft`

### Phase 1.2 — Куски бревна
- **Что:** бревна больше не падают с деревьев — падают «куски бревна»
  (1-2 штуки + 5% палки), 2 куска → 1 доска, прочность primitive_axe 30→8.
- **Коммит:** `ebc0039`

### Phase 1.3 — Хардкорный мир
- **Что:** день ×1.5, сырое мясо/рыба → Hunger I 12с, +30% мобов ночью,
  мобы +15%HP/+10%damage.
- **Коммит:** `7e3bbd3`

### Phase 1.4 — Игрок-инвалид
- **Что:** HP cap 5 сердец, бег запрещён через ускоренный голод
  (×6 расход выше foodLevel=6, потом ваниль), фантомы после сна,
  20% шанс не заснуть, золотые яблоки без эффектов, зомби +20% скорости.
- **Коммиты:** `56c7bde`, `1f64f6c`

### Phase 1.5 — Сырая еда наносит урон
- **Что:** -0.5 сердца на каждое сырое мясо/рыбу, плюс к Hunger I из 1.3.
- **Коммит:** `9094727`

### Phase 1.6 — Окружение и боёвка
- **Что:** падение ×1.5, дождь -0.5/10с, ночной холод -0.5/30с при свете≤7,
  зомби-захват Slowness II 30%, стрелы скелетов ×1.5.
- **Коммит:** `a408970`

### Phase 1.7 — Агрессивные големы + запрет торговли
- **Что:** железные големы атакуют игрока в радиусе 32 блоков
  (через LivingTickEvent + setTarget, минуя goal-систему),
  правый клик по AbstractVillager отменяется (нет торговли).
- **Коммиты:** `3eddd6a`, `a3297f4`

### Phase 1.8 — Дикие волки
- **Что:** не приручённые волки атакуют игрока в радиусе 16 блоков.
- **Коммит:** `0f515b2`

### Phase 1.9 — Лодки и кислород
- **Что:** лодки ×0.91 каждый тик (≈ ×2 медленнее в установке),
  кислород -2/тик вместо -1.
- **Коммит:** `757a305`

### Phase 1.10 — Передвижение
- **Что:** sneak ×0.5, снег -25%, лёд скользкий, encumbrance >10 предметов,
  плавание -30%, лестница ×0.7.
- **Коммит:** `7d6cc3e`

### Phase 1.11 — Хищники
- **Что:** эндермены агрятся на 16 блоков без визуального контакта,
  тихие криперы 15%, ghasts +огнешары, headless creeper, husk вместо зомби 10%.
- **Коммит:** `f28780b`

### Phase 1.12 — Окружение
- **Что:** молнии ×3, жара пустыни, холод снежных биомов,
  лава ×1.5 + 10с огонь, ночные метеоры.
- **Коммит:** `4150a7c`

### Phase 1.13 — Перцепция и физиология
- **Что:** дождь Blindness pulses, zombie infection 15%, death fever 5 мин,
  sleep deprivation после 2 ночей.
- **Коммит:** `a862278`

### Phase 1.14 — Мир и предметы
- **Что:** XP keep 50% при смерти, XP orb 30с, swamp slow, cactus ×2,
  sweet berries ×3.
- **Коммит:** `1253d1a`

### Phase 1.10-1.13 fix
- **Что:** encumbrance threshold 10, удалён эндермен-агро + rain-fog
  (по запросу пользователя), ведьмы везде, фикс жары/холода.
- **Коммит:** `1aeff4c`

### Phase 1.15 — Опасные глубокие пещеры (УДАЛЕНО)
- **Что было:** Darkness/Weakness в глубоких пещерах + редкие cave-ambush.
- **Почему удалено:** пользователь не подтвердил работу, решил что не нужно.
- **Коммит (откат):** `dd23b08` → откатано.

### Phase 2.0 — Запрет ванильных доспехов
- **Что:** запрет крафта ванильных шлема/нагрудника/штанов/ботинок.
- **Коммиты:** `6f9e9d1`, `6551540` (фикс client/swap)

### Phase 2.1 — Запрет ванильных инструментов
- **Что:** через recipe-overrides отключены ванильные рецепты топоров,
  кирок, мечей, лопат, мотыг, луков, арбалетов, кремень-и-стали, щитов
  (для wood/stone/iron/gold/diamond/netherite). Вместо них — модовые crude_*.
- **Коммит:** `063403f`
- **Ветка:** `devin/1777925074-phase-1-1-bushcraft` (несмотря на название)

### Phase 2.2 — Каменные инструменты GTW
- **Что:** 5 предметов `stone_pickaxe/axe/sword/shovel/hoe` + `GTW_STONE` тир
  (level 1, +1 dmg, durability 70, speed 3.0). Рецепты на жгуте.
- **Коммит:** `9abf474`
- **Ветка:** `devin/1778177991-phase-2-2-stone-tools`
- **PR:** [#3](https://github.com/gghyrmrwf/gleb-than-wolves/pull/3)

### Phase 2.2.x — Деревянные + железные инструменты GTW
- **Что:** 10 предметов `wooden_*` (GTW_WOODEN, level 0, durability 30,
  speed 1.5) и `iron_*` (GTW_IRON, level 2, durability 130, speed 4.5).
- **Коммит:** `455125f`
- **Ветка:** `devin/1778180889-phase-2-2-x-wooden-iron-tools`
- **PR:** [#5](https://github.com/gghyrmrwf/gleb-than-wolves/pull/5)
- **Это база для всего, что после.** Если откат — откатываемся сюда.

### Phase 2.2.y — Алмазные инструменты GTW
- **Что:** 5 предметов `diamond_*` + `GTW_DIAMOND` тир (level 3, durability 780,
  speed 6.0). Рецепты: 3 алмаза + 2 жгута.
- **Коммит:** `71bc65a` (исходный) → cherry-pick в текущую ветку как `d61ac87`.
- **Ветка:** `devin/1778187911-phase-2-2-y-diamond-tools`
- **PR:** [#6](https://github.com/gghyrmrwf/gleb-than-wolves/pull/6) — заменён PR #9.

### Phase 2.3a — Mining-gate scaffold (УДАЛЁН)
- **Что было:** добавлен default-deny whitelist через `MiningGate.java`,
  пустые теги `breakable_by/{hand,primitive,stone,iron,diamond,netherite}`.
- **Коммит:** `b0aed17`
- **Ветка:** `devin/1778180196-phase-2-3a-mining-gate`
- **PR:** [#4](https://github.com/gghyrmrwf/gleb-than-wolves/pull/4)
- **ПРОБЛЕМА:** при пустых тегах вообще ничего не ломается. Был задумкой
  под Phase 2.3b — заполнить теги когда появятся осколки.
- **Удалён в Phase 2.3** (см. ниже).

### Phase 2.3b — Система осколков 18 категорий (КАТАСТРОФИЧЕСКИЙ ПРОВАЛ)
- **Что было:** 18 категорий осколков (dirt_chunk, sand_pile, snow_chunk,
  stone_fragment, deepstone_fragment, nether_fragment, brick_fragment,
  hard_brick_fragment, glass_shard, ceramic_piece, concrete_dust, iron_fragment,
  gold_fragment, emerald_fragment, diamond_fragment, quartz_fragment,
  ancient_fragment, compressed_metal_fragment, precious_fragment, machine_scrap,
  wood_chip), 151 рецепт recombine, GLM-инжекция дропов.
- **Дизайн:** [MINING_DESIGN.md](https://github.com/gghyrmrwf/gleb-than-wolves/blob/devin/1778192365-phase-2-3b-impl/MINING_DESIGN.md)
- **Коммиты (хронология):** `e0d6002`, `7d4dce6`, `ebc5928`, `3a72710`,
  `75a4afa`, `baa61fb`, `f905406`, `7899c97`, `33cbb9a`, `9d0b88b`.
- **Ветки:**
  - `devin/1778188321-phase-2-3b-design` (только дизайн-документ)
  - `devin/1778192365-phase-2-3b-impl` (код)
- **PR-ы:** [#7](https://github.com/gghyrmrwf/gleb-than-wolves/pull/7) (design),
  [#8](https://github.com/gghyrmrwf/gleb-than-wolves/pull/8) (impl) —
  **оба отзываются, заменены PR #9.**

#### Что не получилось (важно для следующего ИИ):

1. **Один шард → много рецептов = только первый по алфавиту срабатывает.**
   Майнкрафт-шейпд-рецепт не поддерживает «выбор» выхода. Если у шарда
   13 рецептов (`from_stone_fragment_andesite.json`,
   `from_stone_fragment_brick_stairs.json`, ...) — игра берёт первый
   в алфавитном порядке, остальные 12 мёртвые. Игроку всегда выходит
   `andesite` из stone_fragment, и всё.
   **Урок:** один шард = один очевидный выход (`stone_fragment → cobblestone`,
   `dirt_chunk → dirt`, и т.д.). Если нужен выбор — отдельная станция-«дробилка»
   с GUI, это уже большая работа.

2. **Перегруженные категории нелогичны.** «compressed_metal_fragment» был
   общим осколком для рельсов / хопперов / железных и золотых блоков /
   алмазных блоков → выход `raw_gold` ore, что не имеет смысла.
   **Урок:** один шард = один тип материала (стиль `iron_fragment` вместо
   `compressed_metal_fragment`).

3. **Default-deny whitelist убивает игру.** MiningGate.java выставлял
   destroy speed = 0 для не-whitelisted блоков. Даже если логика верная,
   на практике пользователь жалуется «вообще ничего не добывается».
   **Урок:** не использовать default-deny. Использовать ванильные правила:
   неправильный инструмент → блок ломается, дроп не падает (silently).

4. **Слишком большой PR.** PR #8 — 250 файлов, 5000 LOC за раз. Невозможно
   валидировать инкрементально. **Урок:** делать осколки по одному, с
   подтверждением пользователя на каждом шаге.

5. **Технические баги:**
   - `MissingMappingsEvent` имеет неправильный пакет: правильный
     `net.minecraftforge.registries`, не `net.minecraftforge.event` (было в
     1.18, в 1.20.1 переехало).
   - `MissingMappingsEvent` живёт на FORGE-шине, не MOD-шине. Аннотация
     должна быть `@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)`.

#### Реакция пользователя:

> «полный мусор без логики, напутал крафты, сделал не нужные рецепты,
> вырезал 80% майнкрафт и даже не смог сделать механику добычи блоков,
> максимальный бред и халатность»

Пользователь явно потребовал **полный откат** до состояния PR #5
(после железных инструментов, до алмазных, без всяких осколков).

### Phase 2.3 — Tier tightening через ванильные теги (АКТУАЛЬНОЕ СОСТОЯНИЕ)

После провала 2.3b, пользователь предложил:
- **Иерархия добычи как в ванили**, но **слегка ужесточить** некоторые
  блоки чтобы заставить игрока пройти полный цикл инструментов.
- **Никаких осколков пока что.**
- **Никакого whitelist.** Просто ванильное «не та кирка → нет дропа».

#### Что сделано:

1. **Удалён `MiningGate.java`** + все теги `breakable_by/*` (5 файлов).
2. **Cherry-pick алмазных инструментов** из PR #6.
3. **Датапак-оверрайды на ванильные теги** (`data/minecraft/tags/blocks/...`):

   **`needs_iron_tool` (требуют железную кирку):**
   - 🟧 `copper_ore`, `deepslate_copper_ore` (было stone)
   - 🟦 `lapis_ore`, `deepslate_lapis_ore` (было stone)
   - 🪙 `nether_gold_ore` (было wood)
   - 🟪 `amethyst_block`, `budding_amethyst`, `amethyst_cluster`,
       `large_amethyst_bud`, `medium_amethyst_bud`, `small_amethyst_bud`
       (было wood)
   - 🌌 `end_stone`, `end_stone_bricks`, `end_stone_brick_stairs`,
       `end_stone_brick_slab`, `end_stone_brick_wall` (было wood)
   - 🔔 `bell` (было wood)
   - ⚒️ `anvil`, `chipped_anvil`, `damaged_anvil` (было wood)
   - 📦 `shulker_box` + 16 цветных вариантов (было wood)

   **`needs_stone_tool` (требуют каменную кирку):**
   - 🟦 `nether_quartz_ore` (было wood)
   - 🔥 `magma_block` (было wood)

4. **Glowstone НЕ ужесточён** — у него в ваниле нет
   `requiresCorrectToolForDrops`, поэтому добавление в `needs_iron_tool`
   не имеет эффекта (всё равно дропает пыль голыми руками). Без миксина
   не починить, оставлено как есть.

- **Ветка:** `devin/1778243030-phase-2-3-tier-tighten`
- **Коммит:** `0290c57`
- **PR:** [#9](https://github.com/gghyrmrwf/gleb-than-wolves/pull/9) —
  **АКТУАЛЬНЫЙ. Заменяет PR #6, #7, #8.**

---

## 4. Текущее состояние мода (что в jar 1.0.0)

### Предметы (всего 21):

**Phase 1.x:**
- `plant_fiber` — растительное волокно (15% с травы)
- `plant_cordage` — жгут (2 волокна → 1 жгут)
- `primitive_axe` — примитивный топор (durability 8)
- `wood_chunk` — кусок бревна (2 куска → 1 доска)

**Phase 2.2 (камень) — 5 шт:**
- `stone_pickaxe`, `stone_axe`, `stone_sword`, `stone_shovel`, `stone_hoe`

**Phase 2.2.x (дерево + железо) — 10 шт:**
- `wooden_pickaxe`, `wooden_axe`, `wooden_sword`, `wooden_shovel`, `wooden_hoe`
- `iron_pickaxe`, `iron_axe`, `iron_sword`, `iron_shovel`, `iron_hoe`

**Phase 2.2.y (алмаз) — 5 шт:**
- `diamond_pickaxe`, `diamond_axe`, `diamond_sword`, `diamond_shovel`, `diamond_hoe`

### Тиры инструментов:

| Тир | Level | Durability | Speed | Bonus dmg | Repair |
|---|---|---|---|---|---|
| `PRIMITIVE_TIER` (топор только) | 0 | 8 | 1.0 | 1.0 | — |
| `GTW_WOODEN` | 0 | 30 | 1.5 | 0.0 | oak_planks |
| `GTW_STONE` | 1 | 70 | 3.0 | 1.0 | cobblestone |
| `GTW_IRON` | 2 | 130 | 4.5 | 2.0 | iron_ingot |
| `GTW_DIAMOND` | 3 | 780 | 6.0 | 3.0 | diamond |

**Прогрессия добычи** (после Phase 2.3 ужесточений):
```
руки → primitive_axe → wood pickaxe (булыжник, уголь)
  → stone pickaxe (железная руда, нэзер-кварц, магма-блок)
  → iron pickaxe (медь, лазурит, нэзер-голд, амулет, энд-стоун, шалкер,
                  наковальня, колокол, золото, редстоун, изумруд, алмаз)
  → diamond pickaxe (обсидиан, ancient_debris)
  → netherite (пока не реализован, placeholder)
```

### Структура файлов (актуальная, на ветке `devin/1778243030-phase-2-3-tier-tighten`):

```
src/main/java/com/gghyrmrwf/glebthanwolves/
├── GlebThanWolves.java                  (точка входа)
├── ModItems.java                        (21 итем)
├── ModCreativeTabs.java
├── ModLootModifiers.java                (только AddItem + MultiplyItem GLM)
├── events/
│   ├── BushcraftBreakEvents.java        (Phase 1.1: запрет ломки)
│   ├── HardcoreEvents.java              (Phase 1.3+, большая часть механик)
│   └── WorldEvents.java                 (Phase 1.3, 1.11, 1.12)
├── glm/
│   ├── AddItemModifier.java
│   └── MultiplyItemModifier.java
└── items/
    ├── PrimitiveAxeItem.java            (Phase 1.1)
    └── GtwTiers.java                    (4 тира: WOODEN/STONE/IRON/DIAMOND)

src/main/resources/
├── META-INF/mods.toml
├── pack.mcmeta
├── assets/glebthanwolves/
│   ├── lang/{en_us.json, ru_ru.json}
│   └── models/item/*.json
└── data/
    ├── forge/loot_modifiers/global_loot_modifiers.json
    ├── glebthanwolves/
    │   ├── loot_modifiers/             (GLM правила Phase 1.x)
    │   └── recipes/                    (рецепты модовых инструментов)
    └── minecraft/
        ├── recipes/                     (overrides для ванильных рецептов)
        └── tags/blocks/
            ├── needs_iron_tool.json     (НОВОЕ: 30+ блоков)
            └── needs_stone_tool.json    (НОВОЕ: 2 блока)
```

---

## 5. План дальше (по словам пользователя)

> «буду делать осколки только важным предметам и по очереди»

**Алгоритм:**

1. Пользователь говорит: «начинаем с *такого-то* блока, осколок такой-то,
   из 4 осколков получается такой-то блок».
2. Следующий ИИ делает **отдельный маленький PR** на ОДИН блок:
   - 1 предмет (`xxx_fragment` или подобное)
   - 1 модель item.json
   - 2 lang-записи (en/ru)
   - 1 рецепт recombine (4 шарда → 1 блок, 2×2 без верстака)
   - 1 GLM JSON (когда блок ломается → 2 осколка)
   - 1 коммит, 1 PR
3. Билдит jar, шлёт пользователю.
4. Пользователь тестит, говорит «ок» / «переделать».
5. Только после «ок» переходит к следующему блоку.

**ПРАВИЛА:**

- **НИКАКИХ больших PR** на 250 файлов. Один блок = один PR.
- **НИКАКИХ общих категорий** типа «compressed_metal» или «precious_fragment».
  Каждый шард — для одного типа материала.
- **НИКАКОГО default-deny whitelist.** Использовать только ванильные
  `needs_*_tool` теги.
- **НИКАКИХ переименований** уже существующих предметов через
  MissingMappingsEvent — слишком хрупко.
- **Тестировать `./gradlew build` перед каждым PR.**
- **Spoken language: русский.** Объяснения, обсуждения, баг-репорты,
  описания PR — всё по-русски (но код и коммит-месседжи на английском
  как принято в проекте).

---

## 6. Технические гайдлайны

### Tier-tag система Forge 1.20.1

Forge использует `TierSortingRegistry` для определения, какой тир
инструмента может ломать какие блоки. Правила:

1. Если тир **зарегистрирован в `TierSortingRegistry`** через
   `TierSortingRegistry.registerTier(tier, name, after, before)` —
   используется tag-based проверка по `BlockTags.NEEDS_*_TOOL`.
2. Если тир **не зарегистрирован** — используется **fallback** по
   `tier.getLevel()`:
   ```
   if (level < 3 && state.is(NEEDS_DIAMOND_TOOL)) return false;
   if (level < 2 && state.is(NEEDS_IRON_TOOL))    return false;
   if (level < 1 && state.is(NEEDS_STONE_TOOL))   return false;
   return true;
   ```

В нашем моде GTW тиры **НЕ зарегистрированы** в TierSortingRegistry.
Используется fallback. На практике работает, потому что мы аккуратно
выставили `getLevel()` (0/1/2/3 для wood/stone/iron/diamond).

### Датапак-оверрайды ванильных тегов

Для добавления блоков в существующий ванильный тег
(`minecraft:needs_iron_tool` и т.д.) — кладём JSON по пути
`src/main/resources/data/minecraft/tags/blocks/<tag_name>.json`:

```json
{
  "replace": false,
  "values": [
    "minecraft:block_id"
  ]
}
```

Forge мерджит это с ванилью. `replace: false` — добавление, не замена.

Для **удаления** из ванильного тега (Forge extension):
```json
{
  "replace": false,
  "values": [],
  "remove": ["minecraft:block_id_to_remove"]
}
```

В нашем случае удалять не нужно: если блок одновременно в `needs_stone_tool`
и `needs_iron_tool`, проверка идёт сверху вниз (diamond → iron → stone →
none), поэтому iron-проверка фильтрует первой. Можно просто добавить в
более высокий тег.

### `requiresCorrectToolForDrops` ловушка

Не все блоки в ванили имеют `requiresCorrectToolForDrops()`. Без этого
флага блок дропает себя любым инструментом, и tag-tightening бесполезен.

**Подтверждённые блоки БЕЗ этого флага** (нельзя ужесточить через теги):
- `glowstone` — всегда дропает glowstone_dust любым инструментом

**Если нужно ужесточить такие блоки** — потребуется миксин или
Forge-event handler, переопределяющий drop-логику.

### Global Loot Modifiers (GLM)

Правильный способ инжектировать дроп. Структура:
- Codec в Java (`AddItemModifier.java`).
- Регистрация в `ModLootModifiers.java`.
- JSON правило в `data/<modid>/loot_modifiers/<name>.json`.
- Запись в `data/forge/loot_modifiers/global_loot_modifiers.json`.

**Не использовать BlockEvent-handlers для дропа** — GLM
правильнее, не дублирует ваниль, уважает silk-touch / fortune.

### Команды разработки

```bash
./gradlew compileJava   # быстро, ~5с
./gradlew build         # полная сборка, ~15-30с
ls -la build/libs/glebthanwolves-1.0.0.jar
```

ИИ **не запускает Minecraft локально** — нет доступа к клиенту,
аккаунту, GUI. Только компиляция + jar-доставка пользователю.

### Git конвенции

- Не пушить в `master`/`main`. Всегда новая ветка.
- Имя ветки: `devin/<unix_timestamp>-<short-name>`.
- Заголовок коммита на английском, строка ≤ 72 символа, описание в теле.
- PR-описание: можно русским/английским смешано, главное — структура
  Summary / Testing checklist / Notes.

---

## 7. Полный список PR-ов

| PR | Заголовок | Статус |
|---|---|---|
| [#1](https://github.com/gghyrmrwf/gleb-than-wolves/pull/1) | Phase 0: Forge 1.20.1 MDK scaffold | merged |
| [#2](https://github.com/gghyrmrwf/gleb-than-wolves/pull/2) | (массивный merge всех Phase 1.x) | merged |
| [#3](https://github.com/gghyrmrwf/gleb-than-wolves/pull/3) | Phase 2.2: stone tools | open, база |
| [#4](https://github.com/gghyrmrwf/gleb-than-wolves/pull/4) | Phase 2.3a: mining gate scaffold | open, отзывается |
| [#5](https://github.com/gghyrmrwf/gleb-than-wolves/pull/5) | Phase 2.2.x: wooden + iron tools | open, база PR #9 |
| [#6](https://github.com/gghyrmrwf/gleb-than-wolves/pull/6) | Phase 2.2.y: diamond tools | open, **заменён PR #9** |
| [#7](https://github.com/gghyrmrwf/gleb-than-wolves/pull/7) | Phase 2.3b: design (MINING_DESIGN.md) | open, **отзывается** |
| [#8](https://github.com/gghyrmrwf/gleb-than-wolves/pull/8) | Phase 2.3b: shard impl (18 категорий) | open, **отзывается** |
| **[#9](https://github.com/gghyrmrwf/gleb-than-wolves/pull/9)** | **Phase 2.3: tier tightening (no whitelist) + diamond tools** | **АКТУАЛЬНЫЙ** |

---

## 8. История критических feedback от пользователя (verbatim)

Сохранено для понимания контекста:

> «не нужно каждый раз делать txt файл с описанием что ты изменил»

> «осколки могут быть из руды, твёрдых блоков, земли и т.д»
> (про логичность семейств шардов)

> «зачем тебе шерсть в осколках, как ты это вообще представляешь? это совсем
> не логично, осколки могут быть из руды, твёрдых блоков, земли и т.д,
> каким боком тут шерсть»

> «полный мусор без логики, напутал крафты, сделал не нужные рецепты,
> вырезал 80% майнкрафт и даже не смог сделать механику добычи блоков
> (на всех блоках походу полный вайт лист), максимальный бред и халатность
> при работе с модом, в такое не то что невозможно играть, на это даже
> бредово смотреть»

> «ладно давай так, сделаем ПОЛНЫЙ откат на момент когда ещё не было
> алмазных инструментов, буду делать осколки только важным предметам и
> по очереди»

> «сделай алмазные инструменты и давай так, иерархию добычи блоков как
> в ванильном майнкрафте для наших модовых инструментов, но только чуть
> ужесточить (главное чтобы это не мешало прохождению) то есть не вайтлист
> по добычи, а просто блок не будет выпадать как в обычном майнкрафте»

> «просто смотри что ломает мод и тогда не делай этого»

---

## 9. Чеклист первых действий следующего ИИ

При получении задачи от пользователя:

1. ☐ Прочитать этот HANDOFF.md полностью.
2. ☐ Прочитать `HISTORY.md`, `CHANGELOG.md`, `ARCHITECTURE.md`, `ROADMAP.md`
   в репо (там детальная история Phase 1.x).
3. ☐ Открыть актуальный PR [#9](https://github.com/gghyrmrwf/gleb-than-wolves/pull/9)
   и понять его diff.
4. ☐ Если задача про осколки — **переспросить пользователя один-в-один,
   какой блок и какой шард.** Никаких догадок.
5. ☐ Если задача про новую механику — обсудить дизайн прежде чем кодить.
6. ☐ Перед коммитом — `./gradlew build`. Убедиться, что компилится.
7. ☐ Не коммитить `MINING_DESIGN.md`, не делать массивные дизайн-доки —
   пользователь прямо сказал не нужно.
8. ☐ Использовать ванильные теги, не whitelist.
9. ☐ Один блок / одна механика = один PR.
10. ☐ Сборку jar + jar-доставку **пользователю в чат** делать после каждого
    PR. Пользователь тестит локально.

---

## 10. Версия и контактные данные

- **Версия мода:** `1.0.0` (Phase 2.3, актуально на 2026-05-08)
- **Актуальный jar:** `build/libs/glebthanwolves-1.0.0.jar` на ветке
  `devin/1778243030-phase-2-3-tier-tighten`
- **Актуальный PR:** https://github.com/gghyrmrwf/gleb-than-wolves/pull/9
- **Forge:** 1.20.1 / 47.x
- **Java:** 17

При возобновлении работы — попросить пользователя последний jar или
скачать с этой ветки и собрать самостоятельно (`./gradlew build`).
