package com.example.game

import android.app.AlertDialog
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.core.view.setMargins
import com.example.game.data.model.Item
import com.example.game.data.model.ItemType
import com.example.game.data.model.ItemRarity

class InventoryManager(
    private val activity: RoguelikeMainActivity,
    private var inventory: MutableList<Item>,
    private var equippedWeapon: Item?,
    private var equippedArmor: Item?,
    private var equippedAccessory: Item?,
    private val maxInventorySize: Int,
    private val onInventoryChanged: (equippedWeapon: Item?, equippedArmor: Item?, equippedAccessory: Item?) -> Unit
) {
    private var currentFilter: ItemFilter = ItemFilter.ALL
    private var currentSort: ItemSort = ItemSort.NEWEST
    
    private lateinit var inventoryLayout: LinearLayout
    private lateinit var tvInventoryTitle: TextView
    private lateinit var itemGridContainer: LinearLayout
    private lateinit var equipmentPanel: LinearLayout
    private lateinit var filterContainer: LinearLayout

    enum class ItemFilter(val displayName: String) {
        ALL("ทั้งหมด"),
        WEAPON("อาวุธ"),
        ARMOR("เกราะ"),
        ACCESSORY("ประดับ"),
        CONSUMABLE("ใช้งาน"),
        MATERIAL("วัตถุดิบ")
    }

    enum class ItemSort(val displayName: String) {
        RARITY("ระดับ"),
        TYPE("ประเภท"),
        NAME("ชื่อ"),
        NEWEST("ใหม่สุด")
    }

    fun showInventory() {
        createInventoryUI()
    }

    private fun createInventoryUI() {
        inventoryLayout = LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.parseColor("#121212"))
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }

        addInventoryHeader()
        addEquipmentPanel()
        addFilterAndSort()
        addItemGrid()
        addBottomControls()

        val scrollView = ScrollView(activity).apply {
            setBackgroundColor(Color.parseColor("#121212"))
            isVerticalScrollBarEnabled = false
            addView(inventoryLayout)
        }

        activity.setContentView(scrollView)
    }

    private fun addInventoryHeader() {
        val header = LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.parseColor("#121212"))
            val p = activity.dp(20)
            setPadding(p, p, activity.dp(16), 0)
        }

        tvInventoryTitle = TextView(activity).apply {
            text = "🎒 INVENTORY (${inventory.size}/$maxInventorySize)"
            textSize = 22f
            setTextColor(Color.WHITE)
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
        }
        header.addView(tvInventoryTitle)

        val subTitle = TextView(activity).apply {
            text = "${inventory.size} / $maxInventorySize ITEMS"
            textSize = 12f
            setTextColor(Color.parseColor("#444444"))
            typeface = Typeface.MONOSPACE
            gravity = Gravity.CENTER
            setPadding(0, activity.dp(4), 0, 0)
        }
        header.addView(subTitle)

        inventoryLayout.addView(header)
    }

    private fun addEquipmentPanel() {
        val index = inventoryLayout.childCount
        addEquipmentPanelAtIndex(index)
    }

    private fun createEquipmentSlot(
        emoji: String,
        label: String,
        equippedItem: Item?,
        slotType: ItemType
    ): CardView {
        val slotCard = CardView(activity).apply {
            radius = activity.dp(12).toFloat()
            cardElevation = 0f
            setCardBackgroundColor(Color.parseColor("#1e1e1e"))
        }
        
        val slotLayout = LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            val p = activity.dp(8)
            setPadding(p, p, p, p)
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }
        
        if (equippedItem != null) {
            val itemEmoji = TextView(activity).apply {
                text = equippedItem.emoji
                textSize = 36f
                gravity = Gravity.CENTER
            }
            slotLayout.addView(itemEmoji)
            
            val itemName = TextView(activity).apply {
                text = equippedItem.name
                textSize = 11f
                setTextColor(Color.parseColor("#888888"))
                gravity = Gravity.CENTER
                maxLines = 1
                setPadding(activity.dp(4), activity.dp(4), activity.dp(4), 0)
            }
            slotLayout.addView(itemName)
            
            val indicator = View(activity).apply {
                layoutParams = LinearLayout.LayoutParams(activity.dp(20), activity.dp(2)).apply { topMargin = activity.dp(4) }
                setBackgroundColor(Color.parseColor("#555555"))
            }
            slotLayout.addView(indicator)

            slotLayout.setOnClickListener {
                showUnequipDialog(equippedItem, slotType)
            }
        } else {
            val emptyEmoji = TextView(activity).apply {
                text = emoji
                textSize = 32f
                alpha = 0.1f
                gravity = Gravity.CENTER
            }
            slotLayout.addView(emptyEmoji)
            
            val emptyLabel = TextView(activity).apply {
                text = label.uppercase()
                textSize = 9f
                setTextColor(Color.parseColor("#333333"))
                gravity = Gravity.CENTER
                setPadding(0, activity.dp(4), 0, 0)
                typeface = Typeface.MONOSPACE
            }
            slotLayout.addView(emptyLabel)
        }
        
        slotCard.addView(slotLayout)
        return slotCard
    }

    private fun createStatsDisplay(): View {
        val layout = LinearLayout(activity).apply { orientation = LinearLayout.VERTICAL }
        val statList = listOf("HP", "Attack", "Defense", "Luck")

        statList.forEach { name ->
            val equipBonus = getEquipmentBonus(name)
            val effectKey = when(name) {
                "HP" -> "hp_bonus"
                "Attack" -> "atk_bonus"
                "Defense" -> "def_bonus"
                "Luck" -> "luck_bonus"
                else -> ""
            }
            val skillBonus = if (effectKey.isNotEmpty()) activity.calculateSkillBonus(effectKey) else 0
            val totalBonus = equipBonus + skillBonus
            
            val baseValue = when(name) {
                "HP" -> activity.maxHp
                "Attack" -> activity.atk
                "Defense" -> activity.def
                "Luck" -> activity.luck
                else -> 0
            }
            
            val row = LinearLayout(activity).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply { bottomMargin = activity.dp(4) }
                gravity = Gravity.CENTER_VERTICAL
            }

            val icon = TextView(activity).apply {
                text = when(name) {
                    "HP" -> "❤️ "
                    "Attack" -> "⚔️ "
                    "Defense" -> "🛡️ "
                    "Luck" -> "🍀 "
                    else -> ""
                }
                textSize = 12f
            }
            row.addView(icon)

            val label = TextView(activity).apply {
                text = name
                textSize = 13f
                setTextColor(Color.parseColor("#888888"))
                layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
            }
            row.addView(label)

            val valueText = TextView(activity).apply {
                setTextColor(Color.WHITE)
                val totalValue = baseValue + totalBonus
                
                // Format: Total (Base + Bonus)
                val bonusSign = if (totalBonus >= 0) "+" else ""
                val mainText = "$totalValue ($baseValue "
                val bonusText = "$bonusSign$totalBonus"
                val closingText = ")"
                
                val fullText = "$mainText$bonusText$closingText"
                val spannable = SpannableString(fullText)
                
                // Color the "(Base + Bonus)" part or just the "Bonus" part?
                // Request says: formatting "Total (Base + Bonus)"
                // Usually we color the bonus part to show its impact.
                val bonusStart = mainText.length
                val bonusEnd = mainText.length + bonusText.length
                val color = if (totalBonus > 0) Color.parseColor("#4CAF50") 
                            else if (totalBonus < 0) Color.parseColor("#F44336")
                            else Color.parseColor("#888888")
                
                spannable.setSpan(ForegroundColorSpan(color), bonusStart, bonusEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                
                text = spannable
                textSize = 14f
                typeface = Typeface.MONOSPACE
                gravity = Gravity.END
            }
            row.addView(valueText)

            layout.addView(row)
        }
        return layout
    }

    private fun getEquipmentBonus(stat: String): Int {
        var total = 0
        listOfNotNull(equippedWeapon, equippedArmor, equippedAccessory).forEach { item ->
            total += item.getStat(stat)
        }
        return total
    }

    private fun addFilterAndSort() {
        val index = inventoryLayout.childCount
        addFilterAndSortAtIndex(index)
    }

    private fun createFilterButton(filter: ItemFilter): Button {
        val isActive = currentFilter == filter
        return Button(activity).apply {
            text = filter.displayName
            textSize = 12f
            isAllCaps = false
            setPadding(activity.dp(16), activity.dp(8), activity.dp(16), activity.dp(8))
            
            val background = GradientDrawable().apply {
                cornerRadius = activity.dp(12).toFloat()
                if (isActive) {
                    setColor(Color.parseColor("#333333"))
                    setStroke(activity.dp(2), Color.parseColor("#888888"))
                } else {
                    setColor(Color.parseColor("#1a1a1a"))
                    setStroke(activity.dp(1), Color.parseColor("#222222"))
                }
            }
            this.background = background
            
            setTextColor(if (isActive) Color.WHITE else Color.parseColor("#666666"))
            
            if (isActive) {
                typeface = Typeface.DEFAULT_BOLD
            }
            
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                activity.dp(40)
            ).apply {
                marginEnd = activity.dp(8)
            }
            
            setOnClickListener {
                currentFilter = filter
                refreshInventoryDisplay()
            }
        }
    }

    private fun createSortButton(sort: ItemSort): Button {
        val isActive = currentSort == sort
        return Button(activity).apply {
            text = sort.displayName
            textSize = 12f
            isAllCaps = false
            setPadding(activity.dp(16), activity.dp(8), activity.dp(16), activity.dp(8))
            
            val background = GradientDrawable().apply {
                cornerRadius = activity.dp(12).toFloat()
                if (isActive) {
                    setColor(Color.parseColor("#333333"))
                    setStroke(activity.dp(2), Color.parseColor("#888888"))
                } else {
                    setColor(Color.parseColor("#1a1a1a"))
                    setStroke(activity.dp(1), Color.parseColor("#222222"))
                }
            }
            this.background = background
            
            setTextColor(if (isActive) Color.WHITE else Color.parseColor("#666666"))
            
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                activity.dp(40)
            ).apply {
                marginEnd = activity.dp(8)
            }
            
            setOnClickListener {
                currentSort = sort
                refreshInventoryDisplay()
            }
        }
    }

    private fun addItemGrid() {
        itemGridContainer = LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
            val p = activity.dp(8)
            setPadding(p, p, p, p)
        }
        
        refreshItemGrid()
        inventoryLayout.addView(itemGridContainer)
    }

    private fun refreshItemGrid() {
        itemGridContainer.removeAllViews()
        
        var filteredItems = when(currentFilter) {
            ItemFilter.ALL -> inventory
            ItemFilter.WEAPON -> inventory.filter { it.type == ItemType.WEAPON }
            ItemFilter.ARMOR -> inventory.filter { it.type == ItemType.ARMOR }
            ItemFilter.ACCESSORY -> inventory.filter { it.type == ItemType.ACCESSORY }
            ItemFilter.CONSUMABLE -> inventory.filter { it.type == ItemType.CONSUMABLE }
            ItemFilter.MATERIAL -> inventory.filter { it.type == ItemType.MATERIAL }
        }

        filteredItems = when(currentSort) {
            ItemSort.RARITY -> filteredItems.sortedByDescending { it.rarity.ordinal }
            ItemSort.TYPE -> filteredItems.sortedBy { it.type.name }
            ItemSort.NAME -> filteredItems.sortedBy { it.name }
            ItemSort.NEWEST -> filteredItems.reversed()
        }

        if (filteredItems.isEmpty()) {
            val emptyText = TextView(activity).apply {
                text = "\nไม่มีไอเทมในหมวดนี้\n"
                setTextColor(Color.GRAY)
                gravity = Gravity.CENTER
            }
            itemGridContainer.addView(emptyText)
            return
        }

        filteredItems.forEach { item ->
            itemGridContainer.addView(createItemCard(item))
        }
        
        val spacer = View(activity).apply {
            layoutParams = LinearLayout.LayoutParams(1, activity.dp(100))
        }
        itemGridContainer.addView(spacer)
    }

    private fun createItemCard(item: Item): CardView {
        val isEquipped = item == equippedWeapon || item == equippedArmor || item == equippedAccessory
        
        val card = activity.createCard().apply {
            setCardBackgroundColor(Color.parseColor("#181818"))
            radius = activity.dp(8).toFloat()
            cardElevation = 2f
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(activity.dp(8), activity.dp(4), activity.dp(8), activity.dp(4))
            }
            
            // Border based on rarity or equipment status
            val border = GradientDrawable().apply {
                setColor(Color.parseColor("#181818"))
                cornerRadius = activity.dp(8).toFloat()
                if (isEquipped) {
                    setStroke(activity.dp(2), Color.WHITE)
                } else {
                    // Subtle rarity-colored border for non-common items
                    if (item.rarity != ItemRarity.COMMON) {
                        setStroke(activity.dp(1), item.rarity.color)
                    } else {
                        setStroke(activity.dp(1), Color.parseColor("#222222"))
                    }
                }
            }
            background = border
        }

        val layout = LinearLayout(activity).apply {
            orientation = LinearLayout.HORIZONTAL
            val p = activity.dp(12)
            setPadding(p, p, p, p)
            gravity = Gravity.CENTER_VERTICAL
        }

        // Icon Container with rarity background glow
        val iconContainer = FrameLayout(activity).apply {
            layoutParams = LinearLayout.LayoutParams(activity.dp(48), activity.dp(48))
            val glow = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(item.rarity.color)
                alpha = 30 // Soft glow
            }
            background = glow
        }

        val icon = TextView(activity).apply {
            text = item.emoji
            textSize = 28f
            gravity = Gravity.CENTER
            layoutParams = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        }
        iconContainer.addView(icon)
        layout.addView(iconContainer)

        val infoLayout = LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(activity.dp(12), 0, 0, 0)
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
        }

        // Name and Rarity Badge Row
        val topRow = LinearLayout(activity).apply { 
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        
        val name = TextView(activity).apply {
            text = item.name
            textSize = 15f
            setTextColor(if (item.rarity.ordinal >= ItemRarity.EPIC.ordinal) item.rarity.color else Color.WHITE)
            typeface = Typeface.DEFAULT_BOLD
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
        }
        topRow.addView(name)
        
        val rarityBadge = TextView(activity).apply {
            text = item.rarity.name
            textSize = 8f
            setTextColor(item.rarity.color)
            typeface = Typeface.MONOSPACE
            setPadding(activity.dp(4), activity.dp(1), activity.dp(4), activity.dp(1))
            background = GradientDrawable().apply {
                cornerRadius = activity.dp(4).toFloat()
                setStroke(activity.dp(1), item.rarity.color)
                alpha = 150
            }
        }
        topRow.addView(rarityBadge)
        infoLayout.addView(topRow)

        // Stats with Comparison
        val itemStats = item.getAllStats()
        if (itemStats.isNotEmpty()) {
            val statsLayout = LinearLayout(activity).apply {
                orientation = LinearLayout.HORIZONTAL
                setPadding(0, activity.dp(4), 0, 0)
            }
            
            itemStats.forEach { (key, value) ->
                val statContainer = LinearLayout(activity).apply {
                    orientation = LinearLayout.HORIZONTAL
                    gravity = Gravity.CENTER_VERTICAL
                    setPadding(0, 0, activity.dp(8), 0)
                }
                
                val statIcon = TextView(activity).apply {
                    text = getStatEmoji(key)
                    textSize = 10f
                }
                statContainer.addView(statIcon)
                
                val statValue = TextView(activity).apply {
                    text = " $value"
                    textSize = 11f
                    setTextColor(Color.parseColor("#CCCCCC"))
                }
                statContainer.addView(statValue)
                
                statsLayout.addView(statContainer)
            }
            infoLayout.addView(statsLayout)
        }
        
        // Flavor text / Description
        val description = TextView(activity).apply {
            text = item.description
            textSize = 10f
            setTextColor(Color.parseColor("#666666"))
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
            setPadding(0, activity.dp(2), 0, 0)
            maxLines = 1
            ellipsize = android.text.TextUtils.TruncateAt.END
        }
        infoLayout.addView(description)

        layout.addView(infoLayout)

        if (isEquipped) {
            val equippedIcon = TextView(activity).apply {
                text = "✓"
                textSize = 14f
                setTextColor(Color.WHITE)
                typeface = Typeface.DEFAULT_BOLD
                setPadding(activity.dp(8), 0, 0, 0)
            }
            layout.addView(equippedIcon)
        }

        card.addView(layout)
        card.setOnClickListener { showItemOptions(item) }
        
        return card
    }

    private fun showItemOptions(item: Item) {
        val options = mutableListOf<String>()
        val isEquipped = item == equippedWeapon || item == equippedArmor || item == equippedAccessory
        
        if (item.type == ItemType.WEAPON || item.type == ItemType.ARMOR || item.type == ItemType.ACCESSORY) {
            options.add(if (isEquipped) "Unequip" else "Equip")
        }
        if (item.type == ItemType.CONSUMABLE) {
            options.add("Use")
        }
        options.add("Details")
        options.add("Discard")
        
        AlertDialog.Builder(activity, AlertDialog.THEME_HOLO_DARK)
            .setTitle(item.name)
            .setItems(options.toTypedArray()) { _, which ->
                handleItemOption(item, options[which])
            }
            .show()
    }

    private fun handleItemOption(item: Item, option: String) {
        when(option) {
            "Equip" -> equipItem(item)
            "Unequip" -> unequipItem(item)
            "Use" -> useItem(item)
            "Details" -> showItemInfo(item)
            "Discard" -> confirmDiscard(item)
        }
    }

    private fun equipItem(item: Item) {
        if (item.type == ItemType.WEAPON) {
            val allowedTypes = activity.playerClass.allowedWeaponTypes
            val weaponType = item.weaponType ?: ""
            
            if (weaponType.isNotEmpty() && !allowedTypes.contains(weaponType)) {
                val allowedNames = allowedTypes.joinToString(", ")
                AlertDialog.Builder(activity, AlertDialog.THEME_HOLO_DARK)
                    .setTitle("ไม่สามารถใส่อาวุธนี้ได้")
                    .setMessage("คลาส ${activity.playerClass.displayName} ไม่มีความเชี่ยวชาญในอาวุธประเภทนี้\n\nอาวุธที่ใช้ได้: $allowedNames")
                    .setPositiveButton("ตกลง", null)
                    .show()
                return
            }
            equippedWeapon = item
        } else if (item.type == ItemType.ARMOR) {
            equippedArmor = item
        } else if (item.type == ItemType.ACCESSORY) {
            equippedAccessory = item
        } else {
            return
        }
        
        Toast.makeText(activity, "สวมใส่ ${item.name} แล้ว", Toast.LENGTH_SHORT).show()
        onInventoryChanged(equippedWeapon, equippedArmor, equippedAccessory)
        refreshInventoryDisplay()
    }

    private fun unequipItem(item: Item) {
        when(item.type) {
            ItemType.WEAPON -> if (equippedWeapon == item) equippedWeapon = null
            ItemType.ARMOR -> if (equippedArmor == item) equippedArmor = null
            ItemType.ACCESSORY -> if (equippedAccessory == item) equippedAccessory = null
            else -> return
        }
        Toast.makeText(activity, "Unequipped ${item.name}", Toast.LENGTH_SHORT).show()
        onInventoryChanged(equippedWeapon, equippedArmor, equippedAccessory)
        refreshInventoryDisplay()
    }

    private fun showUnequipDialog(item: Item, type: ItemType) {
        AlertDialog.Builder(activity, AlertDialog.THEME_HOLO_DARK)
            .setTitle("Unequip ${item.name}?")
            .setPositiveButton("Yes") { _, _ -> unequipItem(item) }
            .setNegativeButton("No", null)
            .show()
    }

    private fun useItem(item: Item) {
        val allStats = item.getAllStats()
        // getAllStats() returns keys like "hpRestore", "atk", "teleport", "purify" etc.
        val healAmount = (allStats["hpRestore"] ?: allStats["hp"] ?: 0)
        val isTeleport = (allStats["teleport"] ?: 0) > 0

        when {
            isTeleport -> {
                inventory.remove(item)
                Toast.makeText(activity, "✨ ม้วนคัมภีร์เทเลพอร์ต! ข้ามไปชั้นถัดไป!", Toast.LENGTH_SHORT).show()
                activity.floor++
                activity.enterFloor()
            }
            healAmount > 0 -> {
                activity.heal(healAmount)
                inventory.remove(item)
                Toast.makeText(activity, "ใช้ ${item.name} ฟื้นฟู $healAmount HP!", Toast.LENGTH_SHORT).show()
                onInventoryChanged(equippedWeapon, equippedArmor, equippedAccessory)
                refreshInventoryDisplay()
            }
            else -> {
                Toast.makeText(activity, "ไม่สามารถใช้ไอเทมนี้นอกการต่อสู้ได้", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getStatEmoji(stat: String): String {
        return when(stat.lowercase()) {
            "hp" -> "❤️"
            "attack", "atk" -> "⚔️"
            "defense", "def" -> "🛡️"
            "luck", "luk" -> "🍀"
            else -> "✨"
        }
    }

    private fun showItemInfo(item: Item) {
        val details = StringBuilder()
        details.append(item.description).append("\n\n")
        details.append("Rarity: ").append(item.rarity.name).append("\n")
        details.append("Type: ").append(item.type.name).append("\n\n")
        
        val itemStats = item.getAllStats()
        if (itemStats.isNotEmpty()) {
            details.append("Stats:\n")
            itemStats.forEach { (k, v) ->
                details.append("- ").append(k.uppercase()).append(": +").append(v).append("\n")
            }
        }
        AlertDialog.Builder(activity, AlertDialog.THEME_HOLO_DARK)
            .setTitle(item.name)
            .setMessage(details.toString())
            .setPositiveButton("Close", null)
            .show()
    }

    private fun confirmDiscard(item: Item) {
        if (item == equippedWeapon || item == equippedArmor || item == equippedAccessory) {
            Toast.makeText(activity, "Cannot discard equipped item!", Toast.LENGTH_SHORT).show()
            return
        }
        AlertDialog.Builder(activity, AlertDialog.THEME_HOLO_DARK)
            .setTitle("Discard Item")
            .setMessage("Are you sure you want to discard ${item.name}?")
            .setPositiveButton("Discard") { _, _ ->
                inventory.remove(item)
                refreshInventoryDisplay()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun addBottomControls() {
        val controlLayout = LinearLayout(activity).apply {
            orientation = LinearLayout.HORIZONTAL
            setBackgroundColor(Color.parseColor("#121212"))
            val p = activity.dp(16)
            setPadding(p, p, p, activity.dp(24))
            gravity = Gravity.CENTER
        }
        val btnBack = Button(activity).apply {
            text = "BACK TO ADVENTURE"
            setTextColor(Color.parseColor("#888888"))
            setBackgroundColor(Color.parseColor("#1a1a1a"))
            val p1 = activity.dp(16)
            setPadding(p1, 0, p1, 0)
            layoutParams = LinearLayout.LayoutParams(0, activity.dp(48), 1f).apply { marginEnd = activity.dp(8) }
            setOnClickListener { activity.showGameScreen() }
        }
        val btnSort = Button(activity).apply {
            text = "SORT"
            setTextColor(Color.parseColor("#666666"))
            setBackgroundColor(Color.parseColor("#1a1a1a"))
            layoutParams = LinearLayout.LayoutParams(0, activity.dp(48), 1f)
            setOnClickListener { autoSortInventory() }
        }
        controlLayout.addView(btnBack)
        controlLayout.addView(btnSort)
        inventoryLayout.addView(controlLayout)
    }
    
    private fun autoSortInventory() {
        inventory.sortWith(compareByDescending<Item> { it.rarity.ordinal }.thenBy { it.type.name })
        Toast.makeText(activity, "✅ Inventory sorted!", Toast.LENGTH_SHORT).show()
        refreshInventoryDisplay()
    }
    
    private fun refreshInventoryDisplay() {
        tvInventoryTitle.text = "🎒 INVENTORY (${inventory.size}/$maxInventorySize)"
        
        var equipIndex = -1
        for (i in 0 until inventoryLayout.childCount) {
            val child = inventoryLayout.getChildAt(i)
            if (child is CardView && child.getChildAt(0) == equipmentPanel) {
                equipIndex = i
                break
            }
        }
        if (equipIndex != -1) {
            inventoryLayout.removeViewAt(equipIndex)
            addEquipmentPanelAtIndex(equipIndex)
        }

        val filterIndex = inventoryLayout.indexOfChild(filterContainer)
        if (filterIndex != -1) {
            inventoryLayout.removeViewAt(filterIndex)
            addFilterAndSortAtIndex(filterIndex)
        }
        refreshItemGrid()
    }
    
    private fun addEquipmentPanelAtIndex(index: Int) {
        val equipCard = activity.createCard().apply {
            setCardBackgroundColor(Color.parseColor("#121212"))
            radius = activity.dp(16).toFloat()
            cardElevation = 0f
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                setMargins(activity.dp(12), activity.dp(16), activity.dp(12), activity.dp(8))
            }
            val border = GradientDrawable().apply {
                setColor(Color.parseColor("#121212"))
                cornerRadius = activity.dp(16).toFloat()
                setStroke(activity.dp(1), Color.parseColor("#222222"))
            }
            background = border
        }
        equipmentPanel = LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
            val p = activity.dp(20)
            setPadding(p, p, p, p)
        }
        val titleLayout = LinearLayout(activity).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL }
        val indicator = View(activity).apply {
            layoutParams = LinearLayout.LayoutParams(activity.dp(3), activity.dp(14))
            setBackgroundColor(Color.parseColor("#888888"))
        }
        titleLayout.addView(indicator)
        val title = TextView(activity).apply {
            text = "  EQUIPPED GEAR"
            textSize = 14f
            setTextColor(Color.WHITE)
            typeface = Typeface.DEFAULT_BOLD
            letterSpacing = 0.05f
        }
        titleLayout.addView(title)
        equipmentPanel.addView(titleLayout, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { bottomMargin = activity.dp(20) })
        
        val slotsLayout = LinearLayout(activity).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        }
        slotsLayout.addView(createEquipmentSlot("⚔️", "Weapon", equippedWeapon, ItemType.WEAPON), LinearLayout.LayoutParams(0, activity.dp(110), 1f).apply { marginEnd = activity.dp(10) })
        slotsLayout.addView(createEquipmentSlot("🛡️", "Armor", equippedArmor, ItemType.ARMOR), LinearLayout.LayoutParams(0, activity.dp(110), 1f).apply { marginEnd = activity.dp(10) })
        slotsLayout.addView(createEquipmentSlot("💍", "Accessory", equippedAccessory, ItemType.ACCESSORY), LinearLayout.LayoutParams(0, activity.dp(110), 1f))
        equipmentPanel.addView(slotsLayout)
        
        val statsContainer = LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.TRANSPARENT)
            setPadding(0, activity.dp(16), 0, 0)
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { topMargin = activity.dp(4) }
        }
        val divider = View(activity).apply {
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, activity.dp(1)).apply { bottomMargin = activity.dp(16) }
            setBackgroundColor(Color.parseColor("#222222"))
        }
        statsContainer.addView(divider)
        statsContainer.addView(createStatsDisplay())
        equipmentPanel.addView(statsContainer)
        equipCard.addView(equipmentPanel)
        inventoryLayout.addView(equipCard, index)
    }

    private fun addFilterAndSortAtIndex(index: Int) {
        filterContainer = LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.parseColor("#121212"))
            val p = activity.dp(12)
            setPadding(p, p, p, p)
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                setMargins(activity.dp(8), activity.dp(12), activity.dp(8), activity.dp(4))
            }
        }
        val filterRow = HorizontalScrollView(activity).apply {
            isHorizontalScrollBarEnabled = false
            val inner = LinearLayout(activity).apply { orientation = LinearLayout.HORIZONTAL; setPadding(activity.dp(8), 0, activity.dp(8), 0) }
            inner.addView(TextView(activity).apply { text = "🔍 Filter: "; setTextColor(Color.parseColor("#444444")); textSize = 11f; gravity = Gravity.CENTER; setPadding(0, 0, activity.dp(12), 0) })
            ItemFilter.entries.forEach { inner.addView(createFilterButton(it)) }
            addView(inner)
        }
        val sortRow = HorizontalScrollView(activity).apply {
            isHorizontalScrollBarEnabled = false
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { topMargin = activity.dp(8) }
            val inner = LinearLayout(activity).apply { orientation = LinearLayout.HORIZONTAL; setPadding(activity.dp(8), 0, activity.dp(8), 0) }
            inner.addView(TextView(activity).apply { text = "📊 Sort: "; setTextColor(Color.parseColor("#444444")); textSize = 11f; gravity = Gravity.CENTER; setPadding(0, 0, activity.dp(12), 0) })
            ItemSort.entries.forEach { inner.addView(createSortButton(it)) }
            addView(inner)
        }
        filterContainer.addView(filterRow)
        filterContainer.addView(sortRow)
        inventoryLayout.addView(filterContainer, index)
    }
}
