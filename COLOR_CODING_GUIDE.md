# Color Coding Guide - Live Dashboard Updates

## 🎨 **Visual Status Indicators**

All panels now **refresh every 1 second** for real-time updates!

---

## 📚 **Books Table**

### 🔴 **RED - TAKEN Books**
- **When**: Book has been delivered to user
- **Status**: `TAKEN`
- **Shelf Column**: Shows `[TAKEN]`
- **Color**: Light red background, dark red text

```
Example:
┌────────────────────────────────────────┐
│ 1984  │ George Orwell │ [TAKEN]        │ 🔴 RED
└────────────────────────────────────────┘
```

---

## 🤖 **Robots Table**

### Priority-based coloring (highest priority first):

### 🟡 **YELLOW - Charging**
- **When**: Robot is docked at charging station
- **Status**: "Charging"
- **Battery**: Any % (charging up to 100%)

### 🔴 **RED - Low Battery**
- **When**: Battery < 15% but NOT charging yet
- **Status**: Usually shows "Idle"
- **Action**: Will be sent to charging soon

### 🔵 **BLUE - Busy/Assigned Task**
- **When**: Robot is executing a task
- **Status**: "Executing Task" or "Carrying Book"
- **Duration**: 15 seconds per task
- **Battery**: Drains 5% during execution

### 🟢 **GREEN - Available**
- **When**: Robot is idle and ready for tasks
- **Battery**: >= 15% and not busy
- **Status**: "Idle"

```
Example Robot Table:
┌────────────────────────────────────────────────────────┐
│ ROBOT-1 │ 100.0% │ Idle           │ Empty           │ 🟢 GREEN
│ ROBOT-2 │ 45.0%  │ Executing Task │ 1984            │ 🔵 BLUE
│ ROBOT-3 │ 10.0%  │ Idle           │ Empty           │ 🔴 RED
│ ROBOT-4 │ 55.0%  │ Charging       │ Empty           │ 🟡 YELLOW
│ ROBOT-5 │ 80.0%  │ Carrying Book  │ The Hobbit      │ 🔵 BLUE
└────────────────────────────────────────────────────────┘
```

---

## 🔌 **Charging Stations List**

### 🔴 **RED - All Slots Full**
- **When**: All 3 slots occupied
- **Display**: `CHG-1: Station 1 (3/3)`
- **Meaning**: No charging capacity available

### 🟡 **YELLOW - Partial Occupancy**
- **When**: 1 or 2 slots occupied
- **Display**: `CHG-2: Station 2 (1/3)`
- **Meaning**: Some charging capacity available

### ⚪ **WHITE - All Slots Empty**
- **When**: 0 slots occupied
- **Display**: `CHG-3: Station 3 (0/3)`
- **Meaning**: Full charging capacity available

```
Example Charging Stations:
┌──────────────────────────────────┐
│ CHG-1: Station 1 (3/3)           │ 🔴 RED (full)
│ CHG-2: Station 2 (1/3)           │ 🟡 YELLOW (partial)
│ CHG-3: Station 3 (0/3)           │ ⚪ WHITE (empty)
└──────────────────────────────────┘
```

---

## ⚡ **Battery Management Rules**

### **Task Assignment Check:**
```
IF robot.battery < 15%:
   ❌ REJECT task assignment
   🔴 Robot shows RED
   → Send to charging station
   → Row becomes YELLOW when docked
   
ELSE IF robot.battery >= 20%:
   ✅ ACCEPT task assignment
   🔵 Robot shows BLUE during task
   → Execute task (15 seconds)
   → Battery drains 5%
```

### **Post-Task Check:**
```
Task completes
Battery drains 5%

IF new battery < 15%:
   🔴 Shows RED briefly
   → Immediately sent to charging
   🟡 Shows YELLOW when docked
   
ELSE:
   🟢 Shows GREEN (available for next task)
```

---

## 🔄 **Live Refresh Intervals**

### **Every 1 Second:**
- ✅ Books table (status updates)
- ✅ Robots table (battery, status, current book)
- ✅ Charging stations (slot occupancy)
- ✅ Shelves table (book counts)
- ✅ Tasks table (active tasks)

### **Every 2 Seconds (configurable):**
- ✅ Logs panel (new log entries)

### **Every 500ms:**
- ✅ Stats bar (metrics update)
- ✅ Status bar (status messages)

---

## 🎯 **Complete Task Execution Flow (with Colors)**

### **Example: Getting "1984" Book**

```
Time    Robot     Battery  Status          Color    Book Status
────────────────────────────────────────────────────────────────
00:00   ROBOT-2   100%     Idle            🟢 GREEN  1984: AVAILABLE
00:01   ROBOT-2   100%     Executing Task  🔵 BLUE   1984: IN_TRANSIT
00:02   ROBOT-2   100%     Executing Task  🔵 BLUE   1984: IN_TRANSIT
00:15   ROBOT-2   100%     Executing Task  🔵 BLUE   1984: IN_TRANSIT
00:16   ROBOT-2   95%      Idle            🟢 GREEN  1984: TAKEN 🔴
```

### **Example: Robot Goes to Charging**

```
Time    Robot     Battery  Status          Color    Action
─────────────────────────────────────────────────────────────
00:00   ROBOT-3   20%      Idle            🟢 GREEN  Ready
00:01   ROBOT-3   20%      Executing Task  🔵 BLUE   Task assigned
00:16   ROBOT-3   15%      Idle            🟢 GREEN  Task complete
00:17   ROBOT-3   15%      Executing Task  🔵 BLUE   Next task
00:32   ROBOT-3   10%      Idle            🔴 RED    Battery low!
00:33   ROBOT-3   10%      Charging        🟡 YELLOW Sent to charge
01:43   ROBOT-3   100%     Idle            🟢 GREEN  Fully charged!
```

---

## 📊 **Color Legend Summary**

### **Books:**
- 🔴 **RED** = TAKEN (delivered to user)

### **Robots:**
- 🟢 **GREEN** = Available & ready (battery >= 15%, idle)
- 🔵 **BLUE** = Busy (executing task or carrying book)
- 🔴 **RED** = Low battery (< 15%, not charging yet)
- 🟡 **YELLOW** = Charging (docked at station)

### **Charging Stations:**
- ⚪ **WHITE** = All slots empty
- 🟡 **YELLOW** = Some slots occupied
- 🔴 **RED** = All slots full (no capacity)

---

## 🎮 **Test the Colors:**

1. **Run the application**
2. **Check robots** - all should be 🟢 GREEN (100% battery, idle)
3. **Get a book** - assigned robot turns 🔵 BLUE
4. **Wait 15 seconds** - book turns 🔴 RED when delivered
5. **Check robot battery** - should drop by 5%
6. **Execute 18 tasks** with one robot - it will turn 🔴 RED then 🟡 YELLOW
7. **Watch charging** - robot charges back to 100%, becomes 🟢 GREEN

---

## 🔍 **What to Watch:**

### **Real-time Updates (every 1 second):**
- Robot battery percentages decreasing
- Robot status changing (Idle → Executing → Idle)
- Robot colors changing based on state
- Book status changing (AVAILABLE → IN_TRANSIT → TAKEN)
- Book rows turning red when taken
- Charging station occupancy changing
- Tasks appearing/disappearing from queue

### **Stats Bar (every 500ms):**
- Available robots count
- Busy robots count
- Charging robots count
- Tasks in queue
- Completed tasks

---

**Everything now updates live with clear visual feedback!** 🎨✨

