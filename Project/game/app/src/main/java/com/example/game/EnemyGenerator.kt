package com.example.game

import com.example.game.data.model.ItemRarity
import kotlin.math.roundToInt
import kotlin.random.Random

/**
 * Enemy Generator for Roguelike RPG
 * Generates procedural enemies based on floor level and difficulty
 */

object EnemyGenerator {
    
    // ========== ENEMY TEMPLATES ==========
    
    private val enemyTemplates = listOf(
        // Normal enemies (Floor 1-20) — mix of NORMAL and PACK AI
        EnemyTemplate(
            namePattern = listOf(
                "สไลม์", "ก๊อบลิน", "หนูท่อ", "ค้างคาว", "แมงมุม", "หมาป่า", "โครงกระดูก", "ซอมบี้",
                "ผึ้งป่า", "งูพิษ", "ปูหิน", "วิญญาณแค้น", "ตั๊กแตนตำข้าว", "กระต่ายคลั่ง", "เห็ดพิษ",
                "โจรป่า", "หมาจิ้งจอก", "ตุ๊กแกยักษ์", "หนอนยักษ์", "นกฮูกกลางคืน"
            ),
            emoji = listOf("🟢", "👺", "🐀", "🦇", "🕷️", "🐺", "💀", "🧟", "🐝", "🐍", "🦀", "👻", "🦗", "🐰", "🍄", "🗡️", "🦊", "🦎", "🐛", "🦉"),
            tier = EnemyTier.NORMAL,
            baseHpMultiplier = 1.2,
            baseAtkMultiplier = 1.1,
            baseDefMultiplier = 1.0,
            skills = listOf("โจมตีฉับพลัน", "กัด", "ข่วน", "พ่นพิษ", "ฟื้นฟูพื้นฐาน", "คลั่งโหด"),
            aiTypes = listOf(EnemyAIType.NORMAL, EnemyAIType.PACK, EnemyAIType.AGGRESSIVE),
            minFloor = 1,
            maxFloor = 20
        ),
        // Elite enemies (Floor 10-40) — TACTICAL and AGGRESSIVE
        EnemyTemplate(
            namePattern = listOf(
                "ออร์ค", "อัศวินโครงกระดูก", "จอมเวทย์ดำ", "การ์กอยล์", "ไวเวิร์น", "เกราะผีสิง", "แวมไพร์",
                "โทรลล์ภูเขา", "โกเลมหิน", "มนุษย์หมาป่า", "นักรบลิซาร์ด", "แมนติคอร์", "แมงป่องยักษ์",
                "เพชฌฆาตไร้หัว", "ยักษ์ตาเดียว", "ภูตเงา", "นักบวชปีศาจ", "ทหารม้าโครงกระดูก"
            ),
            emoji = listOf("👹", "💀", "🧙", "🗿", "🐉", "🛡️", "🦇", "🧌", "🪨", "🐺", "🦎", "🦁", "🦂", "🤺", "👁️", "👤", "⛪", "🏇"),
            tier = EnemyTier.ELITE,
            baseHpMultiplier = 1.8,
            baseAtkMultiplier = 1.5,
            baseDefMultiplier = 1.3,
            skills = listOf("ทุบอย่างแรง", "ศรมนตราดำ", "กระแทกโล่", "ดูดชีวิต", "เสียงคำราม", "เกราะป้องกัน", "ทุบเกราะ", "ฟันแม่นยำ"),
            aiTypes = listOf(EnemyAIType.TACTICAL, EnemyAIType.AGGRESSIVE, EnemyAIType.SUPPORT),
            minFloor = 10,
            maxFloor = 40
        ),
        // Champion enemies (Floor 30-60) — BERSERKER and TACTICAL
        EnemyTemplate(
            namePattern = listOf(
                "ปีศาจโลกันตร์", "มังกรไฟ", "ลิชอมตะ", "มิโนทอร์", "ฟีนิกซ์", "คิเมร่า", "บีโฮลเดอร์",
                "ไฮดรา", "ไซคลอปส์", "กริฟฟิน", "ปีศาจเงา", "ยักษ์น้ำแข็ง", "เซอร์เบอรัส", "พัลลาดินตกสวรรค์",
                "ราชาแมงมุม", "ขุนพลปีศาจ", "มังกรโครงกระดูก", "โกเลมลาวา"
            ),
            emoji = listOf("😈", "🐲", "☠️", "🐂", "🔥", "🦁", "👁️", "🐍", "👹", "🦅", "👤", "❄️", "🐕", "⚔️", "🕷️", "🎖️", "🐉", "🌋"),
            tier = EnemyTier.CHAMPION,
            baseHpMultiplier = 2.5,
            baseAtkMultiplier = 1.8,
            baseDefMultiplier = 1.6,
            skills = listOf("เพลิงนรก", "ลมหายใจมังกร", "สัมผัสแห่งตาย", "พุ่งชน", "คลื่นกระแทก", "ฟื้นฟูขั้นสูง", "คลั่งเดือด", "ประหาร", "ดูดชีวิต"),
            aiTypes = listOf(EnemyAIType.BERSERKER, EnemyAIType.TACTICAL, EnemyAIType.AGGRESSIVE),
            minFloor = 30,
            maxFloor = 60
        ),
        // Void enemies (Floor 55-85) — dark/dimensional, TACTICAL + BERSERKER
        EnemyTemplate(
            namePattern = listOf(
                "สัตว์มืดจากมิติว่าง", "เงาสังหาร", "ผู้ทำลายมิติ", "เทพมืดตกสวรรค์", "หมาป่าแห่งความว่างเปล่า",
                "อสูรมิติ", "เงาแห่งหายนะ", "ราชาเงามืด", "ผู้พิทักษ์ความว่างเปล่า", "นักล่าจากอเวจี",
                "มังกรมืดสากล", "ปีศาจแห่งมิติ", "ผีร้ายจากมิติที่เจ็ด", "อสูรกายดำสนิท", "นักรบมิติสูญ"
            ),
            emoji = listOf("🌑", "👤", "🕳️", "😈", "🐺", "🌀", "💀", "👁️", "🌌", "🦂", "🐉", "☠️", "👻", "🔮", "⛓️"),
            tier = EnemyTier.VOID,
            baseHpMultiplier = 3.0,
            baseAtkMultiplier = 2.4,
            baseDefMultiplier = 2.0,
            skills = listOf("ฉีกมิติ", "สาปอมตะ", "ระเบิดมิติ", "กรีดเลือด", "โมฆะจักรวาล", "กระชากจิตใจ", "ปลุกพลังทีม", "ชาร์จพลัง", "ดูดชีวิต"),
            aiTypes = listOf(EnemyAIType.TACTICAL, EnemyAIType.BERSERKER, EnemyAIType.AGGRESSIVE),
            minFloor = 55,
            maxFloor = 85
        ),
        // Ancient enemies (Floor 80-100) — primordial god-like, TACTICAL + BERSERKER
        EnemyTemplate(
            namePattern = listOf(
                "เทพเจ้าดึกดำบรรพ์", "สัตว์ร้ายก่อนกาล", "ยักษ์แห่งจุดเริ่มต้น", "มังกรยุคแรกของโลก",
                "จอมมารจากยุคมืด", "เทพเก่าแก่ที่ตื่นขึ้น", "เอนทิตี้โบราณ", "ผู้ไม่มีชื่อดึกดำบรรพ์",
                "อสูรร้ายจากยุคแรก", "เทพมังกรผู้สร้างโลก", "ราชาผีก่อนกาล", "พระเจ้าผู้ถูกลืม"
            ),
            emoji = listOf("🌋", "🐲", "⚡", "🔱", "🌊", "🌪️", "💥", "🌟", "👑", "🗡️", "🔮", "💎"),
            tier = EnemyTier.ANCIENT,
            baseHpMultiplier = 4.0,
            baseAtkMultiplier = 3.0,
            baseDefMultiplier = 2.5,
            skills = listOf("พิโรธแห่งจักรวาล", "สาปแห่งกาลเวลา", "ตาพระเจ้า", "บดขยี้อัตตา", "คำสาปชั่วนิรันดร์", "ทำลายล้างสัจธรรม", "ฝนดาวตก", "พลังมืดแห่งนิรันดร์", "การลงทัณฑ์"),
            aiTypes = listOf(EnemyAIType.TACTICAL, EnemyAIType.BERSERKER, EnemyAIType.SUPPORT),
            minFloor = 80,
            maxFloor = 100
        ),
        // Boss enemies (Floor 50+) — TACTICAL boss logic
        EnemyTemplate(
            namePattern = listOf(
                "มังกรโบราณ", "จอมมารผู้ยิ่งใหญ่", "อัศวินแห่งความตาย", "สัตว์ร้ายจากขุมนรก",
                "ราชาไร้นาม", "เทพเจ้าผู้เสื่อมสลาย", "มหาเทพมังกร", "จ้าวแห่งความว่างเปล่า",
                "จอมทัพไร้พ่าย", "จักรพรรดิวิญญาณ", "อสูรกายหมื่นปี"
            ),
            emoji = listOf("🐲", "👿", "💀", "🌑", "👑", "🌀", "🐉", "🌌", "🛡️", "👻", "👹"),
            tier = EnemyTier.BOSS,
            baseHpMultiplier = 3.5,
            baseAtkMultiplier = 2.2,
            baseDefMultiplier = 2.0,
            skills = listOf("วันสิ้นโลก", "กลืนกินวิญญาณ", "ดาบมิติว่างเปล่า", "พิโรธ", "มหาภัยพิบัติ", "โล่นิรันดร์", "จุติใหม่", "ทุบเกราะ", "ฟันแม่นยำ", "ฉีกมิติ", "สาปแห่งกาลเวลา", "ตาพระเจ้า"),
            aiTypes = listOf(EnemyAIType.TACTICAL),
            minFloor = 50,
            maxFloor = 999
        )
    )
    
    // ========== GENERATION FUNCTIONS ==========
    
    fun generateEnemy(floor: Int, seed: Long, difficulty: String = "Normal"): Enemy {
        val random = Random(seed)

        val validTemplates = enemyTemplates.filter { floor >= it.minFloor && floor <= it.maxFloor }
        if (validTemplates.isEmpty()) return generateDefaultEnemy(floor, random, difficulty)

        val template = validTemplates.random(random)

        val difficultyMultiplier = when (difficulty) {
            "Easy" -> 0.75; "Normal" -> 1.0; "Hard" -> 1.35; "Nightmare" -> 1.8; else -> 1.0
        }

        val level = (floor + random.nextInt(-1, 3)).coerceAtLeast(1)
        val scaleFactor = 1.0 + ((floor - 1) * 0.18) // 18% per floor (was 15%)

        val maxHp = (40 * scaleFactor * template.baseHpMultiplier * difficultyMultiplier).roundToInt()
        val atk   = (10 * scaleFactor * template.baseAtkMultiplier * difficultyMultiplier).roundToInt()
        val def   = (5  * scaleFactor * template.baseDefMultiplier * difficultyMultiplier).roundToInt()
        val speed = (5 + random.nextInt(-1, 4)).coerceAtLeast(1)

        val baseName = template.namePattern.random(random)
        val suffix = if (random.nextInt(100) < 35) {
            " " + listOf("ระดับสูง", "ขนาดมหึมา", "ต้องสาป", "บ้าคลั่ง", "โบราณ", "จากอเวจี", "วิญญาณ", "กลายพันธุ์", "แบกความแค้น").random(random)
        } else ""
        val name  = baseName + suffix
        val emoji = template.emoji.random(random)

        // Pick 2 distinct skills for each enemy
        val skillPool = template.skills.toMutableList()
        val pick1 = skillPool.random(random).also { skillPool.remove(it) }
        val pick2 = if (skillPool.isNotEmpty()) skillPool.random(random) else pick1
        val skills = generateEnemySkills(pick1, level, random) + generateEnemySkills(pick2, level, random)

        // Pick AI type from template's allowed list
        val aiType = template.aiTypes.random(random)

        val expReward  = (level * 12 * template.tier.expMultiplier).roundToInt()
        val goldReward = (level * 6  * template.tier.goldMultiplier).roundToInt()
        val soulReward = (level * 3  * template.tier.soulMultiplier).roundToInt()

        val loot = generateLoot(floor, template.tier, random)

        val weaknesses = if (random.nextInt(100) < 40) listOf(ElementType.values().random(random)) else emptyList()
        val resistances = if (random.nextInt(100) < 30) {
            listOf(ElementType.values().filter { it !in weaknesses }.random(random))
        } else emptyList()

        return Enemy(
            id = "enemy_${floor}_${seed}",
            name = name, emoji = emoji, level = level,
            hp = maxHp, maxHp = maxHp, atk = atk, def = def, speed = speed,
            expReward = expReward, goldReward = goldReward, soulReward = soulReward,
            skills = skills,
            weaknesses = weaknesses, resistances = resistances,
            lootTable = loot,
            description = generateDescription(baseName, template.tier),
            aiType = aiType
        )
    }

    
    fun generateBossEnemy(floor: Int, seed: Long, difficulty: String = "Normal"): Enemy {
        val random = Random(seed)
        val bossTemplate = enemyTemplates.first { it.tier == EnemyTier.BOSS }

        val difficultyMultiplier = when (difficulty) {
            "Easy" -> 0.8; "Normal" -> 1.0; "Hard" -> 1.6; "Nightmare" -> 2.2; else -> 1.0
        }

        val level = floor + 6
        val scaleFactor = 1.0 + ((floor - 1) * 0.20)

        val maxHp = (120 * scaleFactor * difficultyMultiplier).roundToInt()
        val atk   = (14  * scaleFactor * difficultyMultiplier).roundToInt()
        val def   = (9   * scaleFactor * difficultyMultiplier).roundToInt()
        val speed = 9

        val name  = bossTemplate.namePattern.random(random)
        val emoji = bossTemplate.emoji.random(random)

        // Boss gets 4 unique skills from its pool
        val bossSkillPool = bossTemplate.skills.toMutableList()
        val bossSkills = mutableListOf<EnemySkill>()
        repeat(4) {
            if (bossSkillPool.isNotEmpty()) {
                val pick = bossSkillPool.random(random)
                bossSkillPool.remove(pick)
                bossSkills.addAll(generateEnemySkills(pick, level, random))
            }
        }

        val loot = generateBossLoot(floor, random)

        return Enemy(
            id = "boss_${floor}_${seed}",
            name = "💀 $name 💀", emoji = emoji, level = level,
            hp = maxHp, maxHp = maxHp, atk = atk, def = def, speed = speed,
            expReward = level * 60, goldReward = level * 30, soulReward = level * 12,
            skills = bossSkills,
            weaknesses = emptyList(),
            resistances = listOf(ElementType.PHYSICAL),
            lootTable = loot,
            description = "ผู้ครองชั้นที่ $floor — อันตรายสุดขีด!",
            isBoss = true,
            aiType = EnemyAIType.TACTICAL
        )
    }
    
    private fun generateDefaultEnemy(floor: Int, random: Random, difficulty: String): Enemy {
        val difficultyMultiplier = when (difficulty) {
            "Easy" -> 0.7
            "Normal" -> 1.0
            "Hard" -> 1.3
            "Nightmare" -> 1.7
            else -> 1.0
        }
        
        val level = floor
        val scaleFactor = 1.0 + ((floor - 1) * 0.15)
        
        return Enemy(
            id = "enemy_default_$floor",
            name = "มอนสเตอร์ปริศนา",
            emoji = "👾",
            level = level,
            hp = (25 * scaleFactor * difficultyMultiplier).roundToInt(),
            maxHp = (25 * scaleFactor * difficultyMultiplier).roundToInt(),
            atk = (6 * scaleFactor * difficultyMultiplier).roundToInt(),
            def = (3 * scaleFactor * difficultyMultiplier).roundToInt(),
            speed = 5,
            expReward = level * 10,
            goldReward = level * 5,
            soulReward = level * 2,
            skills = listOf(
                EnemySkill("โจมตี", "⚔️", IntRange(5, 10))
            ),
            lootTable = emptyList()
        )
    }
    
    private fun generateEnemySkills(skillName: String, level: Int, random: Random): List<EnemySkill> {
        return when (skillName) {
            "โจมตีฉับพลัน" -> listOf(
                EnemySkill(
                    name = "โจมตีฉับพลัน",
                    emoji = "⚡",
                    damage = IntRange(level * 2, level * 3),
                    cooldown = 2
                )
            )
            
            "กัด" -> listOf(
                EnemySkill(
                    name = "กัดติดพิษ",
                    emoji = "🦷",
                    damage = IntRange(level * 2, level * 4),
                    effect = StatusEffect.POISON,
                    cooldown = 3
                )
            )

            "ข่วน" -> listOf(
                EnemySkill(
                    name = "ข่วนต่อเนื่อง",
                    emoji = "🐾",
                    damage = IntRange(level * 2, level * 3),
                    cooldown = 2
                )
            )

            "พ่นพิษ" -> listOf(
                EnemySkill(
                    name = "พ่นละอองพิษ",
                    emoji = "🤢",
                    damage = IntRange(level * 2, level * 4),
                    effect = StatusEffect.POISON,
                    cooldown = 4
                )
            )
            
            "ทุบอย่างแรง" -> listOf(
                EnemySkill(
                    name = "ทุบอย่างแรง",
                    emoji = "💥",
                    damage = IntRange(level * 3, level * 5),
                    cooldown = 2
                )
            )
            
            "ศรมนตราดำ" -> listOf(
                EnemySkill(
                    name = "ศรมนตราดำ",
                    emoji = "🌑",
                    damage = IntRange(level * 3, level * 4),
                    effect = StatusEffect.WEAK,
                    cooldown = 3
                )
            )
            
            "กระแทกโล่" -> listOf(
                EnemySkill(
                    name = "กระแทกโล่",
                    emoji = "🛡️",
                    damage = IntRange(level * 2, level * 3),
                    effect = StatusEffect.STUN,
                    cooldown = 4
                )
            )

            "ดูดเลือด" -> listOf(
                EnemySkill(
                    name = "ดูดเลือด",
                    emoji = "🧛",
                    damage = IntRange(level * 3, level * 4),
                    effect = StatusEffect.BLEED,
                    cooldown = 4
                )
            )

            "เสียงคำราม" -> listOf(
                EnemySkill(
                    name = "คำรามข่มขวัญ",
                    emoji = "📢",
                    damage = IntRange(level * 1, level * 2),
                    effect = StatusEffect.WEAK,
                    cooldown = 5
                )
            )
            
            "เพลิงนรก" -> listOf(
                EnemySkill(
                    name = "เพลิงนรก",
                    emoji = "🔥",
                    damage = IntRange(level * 4, level * 6),
                    effect = StatusEffect.BURN,
                    cooldown = 3
                )
            )
            
            "ลมหายใจมังกร" -> listOf(
                EnemySkill(
                    name = "ลมหายใจมังกร",
                    emoji = "🔥",
                    damage = IntRange(level * 5, level * 8),
                    effect = StatusEffect.BURN,
                    cooldown = 4
                )
            )
            
            "สัมผัสแห่งตาย" -> listOf(
                EnemySkill(
                    name = "สัมผัสแห่งตาย",
                    emoji = "☠️",
                    damage = IntRange(level * 3, level * 5),
                    effect = StatusEffect.POISON,
                    cooldown = 3
                )
            )
            
            "พุ่งชน" -> listOf(
                EnemySkill(
                    name = "พุ่งชนรุนแรง",
                    emoji = "💨",
                    damage = IntRange(level * 4, level * 7),
                    effect = StatusEffect.STUN,
                    cooldown = 4
                )
            )

            "สาปเป็นหิน" -> listOf(
                EnemySkill(
                    name = "ดวงตาสาปส่ง",
                    emoji = "👁️",
                    damage = IntRange(level * 3, level * 4),
                    effect = StatusEffect.STUN,
                    cooldown = 5
                )
            )

            "คลื่นกระแทก" -> listOf(
                EnemySkill(
                    name = "คลื่นกระแทก",
                    emoji = "🌊",
                    damage = IntRange(level * 4, level * 5),
                    effect = StatusEffect.DEFENSE_DOWN,
                    cooldown = 4
                )
            )
            
            "วันสิ้นโลก" -> listOf(
                EnemySkill(
                    name = "วันสิ้นโลก",
                    emoji = "💀",
                    damage = IntRange(level * 6, level * 10),
                    effect = StatusEffect.WEAK,
                    cooldown = 5
                )
            )
            
            "กลืนกินวิญญาณ" -> listOf(
                EnemySkill(
                    name = "กลืนกินวิญญาณ",
                    emoji = "👻",
                    damage = IntRange(level * 4, level * 6),
                    effect = StatusEffect.WEAK,
                    cooldown = 3
                )
            )
            
            "ดาบมิติว่างเปล่า" -> listOf(
                EnemySkill(
                    name = "ดาบมิติว่างเปล่า",
                    emoji = "🌑",
                    damage = IntRange(level * 5, level * 8),
                    effect = StatusEffect.DEFENSE_DOWN,
                    cooldown = 4
                )
            )
            
            "พิโรธ" -> listOf(
                EnemySkill(
                    name = "พิโรธ",
                    emoji = "😡",
                    damage = IntRange(level * 7, level * 12),
                    cooldown = 6
                )
            )

            "มหาภัยพิบัติ" -> listOf(
                EnemySkill(
                    name = "มหาภัยพิบัติ",
                    emoji = "⛈️",
                    damage = IntRange(level * 8, level * 15),
                    effect = StatusEffect.BURN,
                    cooldown = 7
                )
            )

            "ฟื้นฟูพื้นฐาน" -> listOf(
                EnemySkill(
                    name = "ฟื้นฟูพื้นฐาน",
                    emoji = "🩹",
                    damage = IntRange(level * 1, level * 2),
                    effect = StatusEffect.REGEN,
                    isSelfTarget = true,
                    cooldown = 5
                )
            )

            "เกราะป้องกัน" -> listOf(
                EnemySkill(
                    name = "สร้างเกราะ",
                    emoji = "🛡️",
                    damage = IntRange(0, 0),
                    effect = StatusEffect.SHIELD,
                    isSelfTarget = true,
                    cooldown = 6
                )
            )

            "ฟื้นฟูขั้นสูง" -> listOf(
                EnemySkill(
                    name = "พรแห่งชีวิต",
                    emoji = "💖",
                    damage = IntRange(level * 3, level * 5),
                    effect = StatusEffect.REGEN,
                    isSelfTarget = true,
                    cooldown = 8
                )
            )

            "โล่นิรันดร์" -> listOf(
                EnemySkill(
                    name = "โล่นิรันดร์",
                    emoji = "🔱",
                    damage = IntRange(0, 0),
                    effect = StatusEffect.SHIELD,
                    isSelfTarget = true,
                    cooldown = 10
                )
            )

            "จุติใหม่" -> listOf(
                EnemySkill(
                    name = "จุติใหม่",
                    emoji = "🕊️",
                    damage = IntRange(level * 10, level * 15),
                    effect = StatusEffect.REGEN,
                    isSelfTarget = true,
                    cooldown = 12
                )
            )
            
            // ─── NEW SKILL GIMMICKS ──────────────────────────────────────────

            // Rally cry: buff all living allies' ATK (SUPPORT AI)
            "ปลุกพลังทีม" -> listOf(
                EnemySkill(
                    name = "ปลุกพลังทีม",
                    emoji = "📣",
                    damage = IntRange(8, 12),
                    effect = StatusEffect.ATK_UP,
                    targetsAllAllies = true,
                    cooldown = 5
                )
            )

            // Armor break: permanently reduce player DEF this fight (TACTICAL AI)
            "ทุบเกราะ" -> listOf(
                EnemySkill(
                    name = "ทุบเกราะทะลุ",
                    emoji = "🔨",
                    damage = IntRange(level * 2, level * 3),
                    effect = StatusEffect.DEFENSE_DOWN,
                    cooldown = 4
                )
            )

            // Vampiric strike: deal damage + heal self (ELITE/CHAMPION)
            "ดูดชีวิต" -> listOf(
                EnemySkill(
                    name = "ดูดพลังชีวิต",
                    emoji = "🩸",
                    damage = IntRange(level * 3, level * 5),
                    lifeSteal = true,
                    cooldown = 3
                )
            )

            // Precise strike: bypass player SHIELD (TACTICAL AI)
            "ฟันแม่นยำ" -> listOf(
                EnemySkill(
                    name = "ฟันแม่นยำทะลุโล่",
                    emoji = "🎯",
                    damage = IntRange(level * 3, level * 4),
                    ignoreShield = true,
                    cooldown = 4
                )
            )

            // Berserker self-buff (BERSERKER AI)
            "คลั่งเดือด" -> listOf(
                EnemySkill(
                    name = "คลั่งเดือด",
                    emoji = "😡",
                    damage = IntRange(0, 0),
                    effect = StatusEffect.ATK_UP,
                    isSelfTarget = true,
                    cooldown = 5
                )
            )

            // Execution blow: +80% dmg when player HP < 30% (CHAMPION/BOSS)
            "ประหาร" -> listOf(
                EnemySkill(
                    name = "ฟันประหาร",
                    emoji = "⚰️",
                    damage = IntRange(level * 4, level * 7),
                    executionBonus = true,
                    cooldown = 6
                )
            )

            // Pack howl: ATK_UP for all allies (PACK AI)
            "คลั่งโหด" -> listOf(
                EnemySkill(
                    name = "โหยหวน",
                    emoji = "🐺",
                    damage = IntRange(6, 10),
                    effect = StatusEffect.ATK_UP,
                    targetsAllAllies = true,
                    cooldown = 5
                )
            )

            // ─── VOID / ANCIENT SKILLS ───────────────────────────────────────

            // Dimension rip: ignoreShield + DEFENSE_DOWN (VOID)
            "ฉีกมิติ" -> listOf(
                EnemySkill(
                    name = "ฉีกมิติทะลุ",
                    emoji = "🕳️",
                    damage = IntRange(level * 4, level * 6),
                    effect = StatusEffect.DEFENSE_DOWN,
                    ignoreShield = true,
                    cooldown = 4
                )
            )

            // Eternal curse: POISON + secondaryEffect BLEED (double DoT)
            "สาปอมตะ" -> listOf(
                EnemySkill(
                    name = "สาปอมตะ",
                    emoji = "💀",
                    damage = IntRange(level * 3, level * 5),
                    effect = StatusEffect.POISON,
                    secondaryEffect = StatusEffect.BLEED,
                    cooldown = 5
                )
            )

            // Void explosion: ignoreShield + BURN (VOID AGGRESSIVE)
            "ระเบิดมิติ" -> listOf(
                EnemySkill(
                    name = "ระเบิดมิติ",
                    emoji = "💥",
                    damage = IntRange(level * 5, level * 7),
                    effect = StatusEffect.BURN,
                    ignoreShield = true,
                    cooldown = 5
                )
            )

            // Claw bleed + lifesteal (VOID)
            "กรีดเลือด" -> listOf(
                EnemySkill(
                    name = "กรีดเลือด",
                    emoji = "🩸",
                    damage = IntRange(level * 3, level * 5),
                    effect = StatusEffect.BLEED,
                    lifeSteal = true,
                    cooldown = 4
                )
            )

            // Void zero: DEFENSE_DOWN + secondaryEffect WEAK (strip everything)
            "โมฆะจักรวาล" -> listOf(
                EnemySkill(
                    name = "โมฆะจักรวาล",
                    emoji = "🌑",
                    damage = IntRange(level * 3, level * 5),
                    effect = StatusEffect.DEFENSE_DOWN,
                    secondaryEffect = StatusEffect.WEAK,
                    cooldown = 5
                )
            )

            // Mind rip: WEAK + ignoreShield (VOID TACTICAL)
            "กระชากจิตใจ" -> listOf(
                EnemySkill(
                    name = "กระชากจิตใจ",
                    emoji = "🧠",
                    damage = IntRange(level * 2, level * 4),
                    effect = StatusEffect.WEAK,
                    ignoreShield = true,
                    cooldown = 4
                )
            )

            // Self ATK charge (BERSERKER self-buff for VOID)
            "ชาร์จพลัง" -> listOf(
                EnemySkill(
                    name = "ชาร์จพลัง",
                    emoji = "⚡",
                    damage = IntRange(0, 0),
                    effect = StatusEffect.ATK_UP,
                    isSelfTarget = true,
                    cooldown = 4
                )
            )

            // Ancient cosmic wrath: BURN + massive damage (ANCIENT AGGRESSIVE)
            "พิโรธแห่งจักรวาล" -> listOf(
                EnemySkill(
                    name = "พิโรธแห่งจักรวาล",
                    emoji = "🌋",
                    damage = IntRange(level * 7, level * 11),
                    effect = StatusEffect.BURN,
                    cooldown = 6
                )
            )

            // Time curse: WEAK + secondaryEffect DEFENSE_DOWN (double debuff)
            "สาปแห่งกาลเวลา" -> listOf(
                EnemySkill(
                    name = "สาปแห่งกาลเวลา",
                    emoji = "⏳",
                    damage = IntRange(level * 3, level * 5),
                    effect = StatusEffect.WEAK,
                    secondaryEffect = StatusEffect.DEFENSE_DOWN,
                    cooldown = 5
                )
            )

            // Eye of god: STUN + ignoreShield (ANCIENT TACTICAL)
            "ตาพระเจ้า" -> listOf(
                EnemySkill(
                    name = "ตาพระเจ้า",
                    emoji = "👁️",
                    damage = IntRange(level * 4, level * 6),
                    effect = StatusEffect.STUN,
                    ignoreShield = true,
                    cooldown = 6
                )
            )

            // Crush ego: executionBonus + lifeSteal (ANCIENT BERSERKER)
            "บดขยี้อัตตา" -> listOf(
                EnemySkill(
                    name = "บดขยี้อัตตา",
                    emoji = "💢",
                    damage = IntRange(level * 5, level * 8),
                    executionBonus = true,
                    lifeSteal = true,
                    cooldown = 7
                )
            )

            // Eternal poison: POISON + secondaryEffect WEAK (ANCIENT)
            "คำสาปชั่วนิรันดร์" -> listOf(
                EnemySkill(
                    name = "คำสาปชั่วนิรันดร์",
                    emoji = "🔮",
                    damage = IntRange(level * 4, level * 6),
                    effect = StatusEffect.POISON,
                    secondaryEffect = StatusEffect.WEAK,
                    cooldown = 6
                )
            )

            // Destroy truth: ignoreShield + DEFENSE_DOWN (ANCIENT TACTICAL)
            "ทำลายล้างสัจธรรม" -> listOf(
                EnemySkill(
                    name = "ทำลายล้างสัจธรรม",
                    emoji = "⚡",
                    damage = IntRange(level * 5, level * 7),
                    effect = StatusEffect.DEFENSE_DOWN,
                    ignoreShield = true,
                    cooldown = 5
                )
            )

            // Meteor rain: high damage + BLEED (ANCIENT)
            "ฝนดาวตก" -> listOf(
                EnemySkill(
                    name = "ฝนดาวตก",
                    emoji = "☄️",
                    damage = IntRange(level * 6, level * 9),
                    effect = StatusEffect.BLEED,
                    cooldown = 6
                )
            )

            // Ancient self ATK-UP (huge buff, ANCIENT BERSERKER)
            "พลังมืดแห่งนิรันดร์" -> listOf(
                EnemySkill(
                    name = "พลังมืดแห่งนิรันดร์",
                    emoji = "🌑",
                    damage = IntRange(0, 0),
                    effect = StatusEffect.ATK_UP,
                    isSelfTarget = true,
                    cooldown = 6
                )
            )

            // Judgment execution: executionBonus + secondaryEffect BURN (ANCIENT)
            "การลงทัณฑ์" -> listOf(
                EnemySkill(
                    name = "การลงทัณฑ์",
                    emoji = "⚖️",
                    damage = IntRange(level * 5, level * 9),
                    secondaryEffect = StatusEffect.BURN,
                    executionBonus = true,
                    cooldown = 7
                )
            )

            // ─── ADDITIONAL GENERAL SKILLS ───────────────────────────────────

            // Lightning: BURN + secondaryEffect STUN
            "ฟ้าผ่า" -> listOf(
                EnemySkill(
                    name = "ฟ้าผ่าสายฟ้า",
                    emoji = "⚡",
                    damage = IntRange(level * 4, level * 6),
                    effect = StatusEffect.BURN,
                    secondaryEffect = StatusEffect.STUN,
                    cooldown = 5
                )
            )

            // War curse: WEAK + secondaryEffect DEFENSE_DOWN
            "คำสาปสงคราม" -> listOf(
                EnemySkill(
                    name = "คำสาปสงคราม",
                    emoji = "🗡️",
                    damage = IntRange(level * 3, level * 4),
                    effect = StatusEffect.WEAK,
                    secondaryEffect = StatusEffect.DEFENSE_DOWN,
                    cooldown = 5
                )
            )

            // Paralysis: STUN + secondaryEffect DEFENSE_DOWN
            "อัมพาต" -> listOf(
                EnemySkill(
                    name = "อัมพาต",
                    emoji = "💫",
                    damage = IntRange(level * 2, level * 4),
                    effect = StatusEffect.STUN,
                    secondaryEffect = StatusEffect.DEFENSE_DOWN,
                    cooldown = 5
                )
            )

            // Squeeze: BLEED + executionBonus
            "บีบคั้น" -> listOf(
                EnemySkill(
                    name = "บีบคั้น",
                    emoji = "🩸",
                    damage = IntRange(level * 4, level * 6),
                    effect = StatusEffect.BLEED,
                    executionBonus = true,
                    cooldown = 5
                )
            )

            // Acid fire: BURN + lifeSteal
            "ไฟกรด" -> listOf(
                EnemySkill(
                    name = "ไฟกรดกัดกิน",
                    emoji = "🔥",
                    damage = IntRange(level * 3, level * 5),
                    effect = StatusEffect.BURN,
                    lifeSteal = true,
                    cooldown = 4
                )
            )

            // Arrow rain: BLEED
            "ฝนลูกศร" -> listOf(
                EnemySkill(
                    name = "ฝนลูกศร",
                    emoji = "🏹",
                    damage = IntRange(level * 3, level * 5),
                    effect = StatusEffect.BLEED,
                    cooldown = 4
                )
            )

            // Energy blast: high pure damage (AGGRESSIVE)
            "ระเบิดพลังงาน" -> listOf(
                EnemySkill(
                    name = "ระเบิดพลังงาน",
                    emoji = "💥",
                    damage = IntRange(level * 6, level * 9),
                    cooldown = 5
                )
            )

            // Stomp: DEFENSE_DOWN + high damage
            "เหยียบย่ำ" -> listOf(
                EnemySkill(
                    name = "เหยียบย่ำ",
                    emoji = "🦶",
                    damage = IntRange(level * 5, level * 7),
                    effect = StatusEffect.DEFENSE_DOWN,
                    cooldown = 4
                )
            )

            // Soul draw: lifeSteal + BLEED
            "ดึงดูดวิญญาณ" -> listOf(
                EnemySkill(
                    name = "ดึงดูดวิญญาณ",
                    emoji = "🌀",
                    damage = IntRange(level * 4, level * 6),
                    effect = StatusEffect.BLEED,
                    lifeSteal = true,
                    cooldown = 5
                )
            )

            // Iron shell: isSelfTarget SHIELD
            "เกราะหุ้มเหล็ก" -> listOf(
                EnemySkill(
                    name = "เกราะหุ้มเหล็ก",
                    emoji = "🛡️",
                    damage = IntRange(0, 0),
                    effect = StatusEffect.SHIELD,
                    isSelfTarget = true,
                    cooldown = 6
                )
            )

            else -> listOf(
                EnemySkill(
                    name = "โจมตี",
                    emoji = "⚔️",
                    damage = IntRange(level * 2, level * 4)
                )
            )
        }
    }
    
    private fun generateLoot(floor: Int, tier: EnemyTier, random: Random): List<LootDrop> {
        val loot = mutableListOf<LootDrop>()
        
        // Consumables
        if (random.nextInt(100) < 40) {
            loot.add(LootDrop(
                item = ItemGenerator.generateConsumable("health_potion", random),
                dropChance = 0.3f
            ))
        }
        
        // Equipment based on tier
        when (tier) {
            EnemyTier.ELITE -> {
                if (random.nextInt(100) < 20) {
                    loot.add(LootDrop(
                        item = ItemGenerator.generateEquipment(floor, ItemRarity.UNCOMMON, random),
                        dropChance = 0.15f
                    ))
                }
            }

            EnemyTier.CHAMPION -> {
                if (random.nextInt(100) < 30) {
                    loot.add(LootDrop(
                        item = ItemGenerator.generateEquipment(floor, ItemRarity.RARE, random),
                        dropChance = 0.2f
                    ))
                }
            }

            EnemyTier.VOID -> {
                if (random.nextInt(100) < 35) {
                    loot.add(LootDrop(
                        item = ItemGenerator.generateEquipment(floor, ItemRarity.EPIC, random),
                        dropChance = 0.25f
                    ))
                }
            }

            EnemyTier.ANCIENT -> {
                loot.add(LootDrop(
                    item = ItemGenerator.generateEquipment(floor, ItemRarity.LEGENDARY, random),
                    dropChance = 0.35f
                ))
                if (random.nextInt(100) < 20) {
                    loot.add(LootDrop(
                        item = ItemGenerator.generateMaterial("boss_soul", random),
                        dropChance = 0.3f
                    ))
                }
            }

            EnemyTier.BOSS -> {
                loot.add(LootDrop(
                    item = ItemGenerator.generateEquipment(floor, ItemRarity.EPIC, random),
                    dropChance = 0.8f
                ))
            }

            else -> {}
        }
        
        return loot
    }
    
    private fun generateBossLoot(floor: Int, random: Random): List<LootDrop> {
        return listOf(
            LootDrop(
                item = ItemGenerator.generateEquipment(floor, ItemRarity.LEGENDARY, random),
                dropChance = 1.0f
            ),
            LootDrop(
                item = ItemGenerator.generateConsumable("elixir", random),
                dropChance = 0.5f
            ),
            LootDrop(
                item = ItemGenerator.generateMaterial("boss_soul", random),
                dropChance = 1.0f
            )
        )
    }
    
    private fun generateDescription(baseName: String, tier: EnemyTier): String {
        return when (tier) {
            EnemyTier.BOSS    -> "ผู้ครองดันเจี้ยน - พลังมหาศาล!"
            EnemyTier.ANCIENT -> "$baseName ผู้มีพลังแห่งการสร้างและทำลาย"
            EnemyTier.VOID    -> "$baseName จากมิติความว่างเปล่า"
            EnemyTier.CHAMPION -> "$baseName ที่แข็งแกร่งเหนือใคร"
            EnemyTier.ELITE   -> "$baseName ที่มีพลังพิเศษ"
            else              -> "$baseName ธรรมดา"
        }
    }
}

// ========== ENEMY TEMPLATE ==========

data class EnemyTemplate(
    val namePattern: List<String>,
    val emoji: List<String>,
    val tier: EnemyTier,
    val baseHpMultiplier: Double,
    val baseAtkMultiplier: Double,
    val baseDefMultiplier: Double,
    val skills: List<String>,
    val aiTypes: List<EnemyAIType> = listOf(EnemyAIType.NORMAL),
    val minFloor: Int,
    val maxFloor: Int
)

enum class EnemyTier(
    val expMultiplier: Double,
    val goldMultiplier: Double,
    val soulMultiplier: Double
) {
    NORMAL(1.0, 1.0, 1.0),
    ELITE(1.5, 1.5, 1.3),
    CHAMPION(2.0, 2.0, 1.5),
    VOID(2.8, 2.8, 2.2),
    ANCIENT(4.0, 4.0, 3.0),
    BOSS(3.0, 3.0, 2.0)
}
