package com.example.game.domain

import com.example.game.data.model.*
import kotlin.random.Random

object CombatEngine {

    data class CombatResult(
        val playerHpDelta: Int = 0,
        val playerManaDelta: Int = 0,
        val playerStaminaDelta: Int = 0,
        val enemyHpDelta: Int = 0,
        val message: String,
        val isCritical: Boolean = false,
        val isEnemyDefeated: Boolean = false,
        val isPlayerDefeated: Boolean = false
    )

    fun playerBasicAttack(player: PlayerStats, enemy: EnemyState, inventory: Inventory): CombatResult {
        val base = player.strength + inventory.totalAtk() + Random.nextInt(1, 9)
        val dmg = maxOf(1, base - enemy.defense)
        val critChance = player.luck + inventory.totalLuck()
        val isCrit = Random.nextInt(100) < critChance
        val finalDmg = if (isCrit) (dmg * 1.6).toInt() else dmg
        val newEnemyHp = enemy.hp - finalDmg

        val weapon = inventory.equippedWeapon
        val msg = if (isCrit) {
            val critVerbs = listOf("ฟาดฟันเข้าจุดตายอย่างรุนแรง", "โจมตีจุดอ่อนอย่างจัง", "ปลดปล่อยการโจมตีอันทรงพลัง")
            "⚡ วิกฤต! คุณ${critVerbs.random()}ใส่ ${enemy.emoji} ${enemy.name} สร้างความเสียหาย $finalDmg หน่วย!"
        } else {
            val weaponVerbs = when {
                weapon == null -> listOf("ต่อย", "เตะ")
                weapon.name.contains("ดาบ", ignoreCase = true) || weapon.name.contains("Sword", ignoreCase = true) -> listOf("ฟัน", "ตวัดดาบใส่")
                weapon.name.contains("ขวาน", ignoreCase = true) || weapon.name.contains("Axe", ignoreCase = true) -> listOf("จามขวานใส่", "เหวี่ยงขวานใส่")
                weapon.name.contains("ธนู", ignoreCase = true) || weapon.name.contains("Bow", ignoreCase = true) -> listOf("ยิงลูกธนูใส่", "ส่องยิง")
                weapon.name.contains("ไม้เท้า", ignoreCase = true) || weapon.name.contains("Staff", ignoreCase = true) -> listOf("หวดไม้เท้าใส่", "ฟาด")
                weapon.name.contains("มีด", ignoreCase = true) || weapon.name.contains("Dagger", ignoreCase = true) || weapon.name.contains("Knife", ignoreCase = true) -> listOf("แทง", "กรีด")
                else -> listOf("โจมตี")
            }
            "⚔️ คุณ${weaponVerbs.random()} ${enemy.emoji} ${enemy.name} สร้างความเสียหาย $finalDmg หน่วย"
        }

        return CombatResult(
            enemyHpDelta = -finalDmg,
            message = msg,
            isCritical = isCrit,
            isEnemyDefeated = newEnemyHp <= 0
        )
    }

    fun playerUseSkill(skill: Skill, player: PlayerStats, enemy: EnemyState): CombatResult {
        if (player.mana < skill.manaCost) {
            return CombatResult(message = "❌ มานาไม่เพียงพอ! (ต้องการ ${skill.manaCost})")
        }
        if (player.stamina < skill.staminaCost) {
            return CombatResult(message = "❌ ความอึดไม่เพียงพอ! (ต้องการ ${skill.staminaCost})")
        }
        if (skill.healing > 0) {
            val healed = minOf(skill.healing, player.maxHp - player.hp)
            val healVerbs = listOf("ใช้คาถาฟื้นฟู", "กระตุ้นพลังชีวิต", "รักษาแผลด้วยเวทมนตร์")
            return CombatResult(
                playerHpDelta = healed,
                playerManaDelta = -skill.manaCost,
                message = "${skill.emoji} ${skill.name}: ${healVerbs.random()}! คุณฟื้นฟู $healed HP!"
            )
        }
        val rawDmg = if (skill.maxDamage > 0) Random.nextInt(skill.minDamage, skill.maxDamage + 1) else skill.minDamage
        val dmg = maxOf(1, rawDmg - enemy.defense / 2)
        val newEnemyHp = enemy.hp - dmg
        
        val attackVerbs = when (skill.id) {
            "fireball" -> listOf("ร่ายบอลเพลิงใส่", "ปลดปล่อยระเบิดเพลิงใส่", "แผดเผาด้วยไฟ")
            "shield_bash" -> listOf("กระแทกโล่ใส่", "ใช้โล่กระแทกหน้า", "ฟาดด้วยโล่")
            "critical_strike" -> listOf("แทงจุดตายของ", "จู่โจมอย่างรุนแรงใส่", "ฟันเข้าที่จุดอ่อนของ")
            "multi_shot" -> listOf("ระดมยิงลูกศรใส่", "สาดกระสุนธนูใส่")
            "divine_judgment" -> listOf("อัญเชิญสายฟ้าศักดิ์สิทธิ์ลงมาที่", "ลงทัณฑ์ด้วยแสงสว่างใส่")
            else -> listOf("ใช้ทักษะ ${skill.name} ใส่", "โจมตีด้วย ${skill.name} ต่อ")
        }
        
        val verb = attackVerbs.random()
        val effectText = when (skill.effect) {
            SkillEffect.STUN -> " + 💫 ติดสตั้น!"
            SkillEffect.POISON -> " + 🤢 ติดพิษ!"
            SkillEffect.BUFF_ATK -> " + ⚔️ พลังโจมตีเพิ่มขึ้น!"
            SkillEffect.BUFF_DEF -> " + 🛡️ พลังป้องกันเพิ่มขึ้น!"
            else -> ""
        }

        return CombatResult(
            playerManaDelta = -skill.manaCost,
            playerStaminaDelta = -skill.staminaCost,
            enemyHpDelta = -dmg,
            message = "${skill.emoji} ${skill.name}: $verb ${enemy.name}! สร้างความเสียหาย $dmg หน่วย$effectText",
            isEnemyDefeated = newEnemyHp <= 0
        )
    }

    fun playerUseItem(item: Item, player: PlayerStats, enemy: EnemyState): CombatResult? {
        if (item.type != ItemType.CONSUMABLE) return null
        val msgs = mutableListOf<String>()
        var hpDelta = 0
        var manaDelta = 0
        if (item.hpRestore > 0) {
            val healed = minOf(item.hpRestore, player.maxHp - player.hp)
            hpDelta = healed
            msgs += "ฟื้นฟู $healed HP"
        }
        if (item.manaRestore > 0) {
            val restored = minOf(item.manaRestore, player.maxMana - player.mana)
            manaDelta = restored
            msgs += "ฟื้นฟู $restored Mana"
        }
        return CombatResult(
            playerHpDelta = hpDelta,
            playerManaDelta = manaDelta,
            message = "${item.emoji} ใช้ ${item.name}: ${msgs.joinToString(", ")}"
        )
    }

    fun enemyAttack(enemy: EnemyState, player: PlayerStats, inventory: Inventory): CombatResult {
        val base = enemy.attack + Random.nextInt(0, 4)
        val dmg = maxOf(1, base - (player.defense + inventory.totalDef()))
        val newPlayerHp = player.hp - dmg

        val enemyVerbs = when {
            enemy.name.contains("สไลม์", ignoreCase = true) || enemy.name.contains("Slime", ignoreCase = true) -> listOf("พุ่งชน", "พ่นเมือกใส่")
            enemy.name.contains("โครงกระดูก", ignoreCase = true) || enemy.name.contains("Skeleton", ignoreCase = true) -> listOf("ฟันด้วยดาบผุๆ", "หวดกระดูกใส่")
            enemy.name.contains("หมาป่า", ignoreCase = true) || enemy.name.contains("Wolf", ignoreCase = true) -> listOf("กระโจนกัด", "ข่วน")
            enemy.name.contains("ก๊อบลิน", ignoreCase = true) || enemy.name.contains("Goblin", ignoreCase = true) -> listOf("แทงด้วยหอก", "ปามีดใส่")
            enemy.name.contains("มังกร", ignoreCase = true) || enemy.name.contains("Dragon", ignoreCase = true) -> listOf("พ่นไฟใส่", "ตะปบ")
            else -> listOf("โจมตี", "จู่โจม")
        }

        val msg = "${enemy.emoji} ${enemy.name} ${enemyVerbs.random()}คุณ สร้างความเสียหาย $dmg หน่วย!"

        return CombatResult(
            playerHpDelta = -dmg,
            message = msg,
            isPlayerDefeated = newPlayerHp <= 0
        )
    }

    fun tryFlee(player: PlayerStats): Pair<Boolean, String> {
        val chance = 40 + player.agility * 5
        return if (Random.nextInt(100) < chance) {
            Pair(true, "💨 คุณหลบหนีเข้าสู่เงามืดและหนีรอดไปได้!")
        } else {
            Pair(false, "❌ หนีไม่พ้น! ศัตรูขวางทางคุณไว้")
        }
    }
}
