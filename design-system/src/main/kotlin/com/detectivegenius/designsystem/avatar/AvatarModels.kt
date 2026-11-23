package com.detectivegenius.designsystem.avatar

enum class AvatarState {
    IDLE,
    SELECTED,
    THINKING,
    TALKING,
    ELIMINATED,
    VICTORY
}

data class AvatarDefinition(
    val id: String,
    val nameEn: String,
    val nameFa: String,
    val emoji: String,
    val primaryColor: Long,
    val secondaryColor: Long,
    val badgeIconName: String,
    val descriptionEn: String,
    val descriptionFa: String
)

object AvatarRegistry {
    val ALL_AVATARS: List<AvatarDefinition> = listOf(
        AvatarDefinition(
            id = "detective",
            nameEn = "Detective",
            nameFa = "کارآگاه",
            emoji = "🕵️",
            primaryColor = 0xFF6366F1L,
            secondaryColor = 0xFF312E81L,
            badgeIconName = "search",
            descriptionEn = "Master of observation and inductive reasoning.",
            descriptionFa = "استاد مشاهده دقیق و استنتاج‌های منطقی."
        ),
        AvatarDefinition(
            id = "magician",
            nameEn = "Magician",
            nameFa = "شعبده‌باز",
            emoji = "🎩",
            primaryColor = 0xFF8B5CF6L,
            secondaryColor = 0xFF4C1D95L,
            badgeIconName = "auto_fix_high",
            descriptionEn = "Keeper of secrets, sleights, and misdirection.",
            descriptionFa = "نگهبان رازها و شعبده‌های غیرقابل پیش‌بینی."
        ),
        AvatarDefinition(
            id = "astronaut",
            nameEn = "Astronaut",
            nameFa = "فضانورد",
            emoji = "🧑‍🚀",
            primaryColor = 0xFF0EA5E9L,
            secondaryColor = 0xFF075985L,
            badgeIconName = "rocket_launch",
            descriptionEn = "Explorer of uncharted cosmic mysteries.",
            descriptionFa = "کاوشگر ناشناخته‌ها و رازهای کهکشانی."
        ),
        AvatarDefinition(
            id = "fox",
            nameEn = "Fox",
            nameFa = "روباه",
            emoji = "🦊",
            primaryColor = 0xFFF97316L,
            secondaryColor = 0xFF9A3412L,
            badgeIconName = "pets",
            descriptionEn = "Cunning investigator who reads between the lines.",
            descriptionFa = "زیرک و چابک در خواندن نیت‌های پنهان."
        ),
        AvatarDefinition(
            id = "owl",
            nameEn = "Owl",
            nameFa = "جغد",
            emoji = "🦉",
            primaryColor = 0xFFA855F7L,
            secondaryColor = 0xFF581C87L,
            badgeIconName = "visibility",
            descriptionEn = "Wise night watcher with unmatched scrutiny.",
            descriptionFa = "دیده‌بان خردمند شب با نگاهی ژرف‌نگر."
        ),
        AvatarDefinition(
            id = "cat",
            nameEn = "Cat",
            nameFa = "گربه",
            emoji = "🐱",
            primaryColor = 0xFFEC4899L,
            secondaryColor = 0xFF831843L,
            badgeIconName = "cruelty_free",
            descriptionEn = "Silent inquisitor slipping through locked doors.",
            descriptionFa = "کارآگاهی آرام که از تمام درهای بسته عبور می‌کند."
        ),
        AvatarDefinition(
            id = "robot",
            nameEn = "Robot",
            nameFa = "ربات",
            emoji = "🤖",
            primaryColor = 0xFF06B6D4L,
            secondaryColor = 0xFF164E63L,
            badgeIconName = "memory",
            descriptionEn = "High-precision computational deduction unit.",
            descriptionFa = "واحد پردازش تحلیلی با محاسبات فوق دقیق."
        ),
        AvatarDefinition(
            id = "ghost",
            nameEn = "Ghost",
            nameFa = "شبح",
            emoji = "👻",
            primaryColor = 0xFF94A3B8L,
            secondaryColor = 0xFF334155L,
            badgeIconName = "blur_on",
            descriptionEn = "Unseen presence listening to confidential whispers.",
            descriptionFa = "حضور نادیدنی که زمزمه‌های محرمانه را می‌شنود."
        ),
        AvatarDefinition(
            id = "artist",
            nameEn = "Artist",
            nameFa = "هنرمند",
            emoji = "🎨",
            primaryColor = 0xFFF43F5EL,
            secondaryColor = 0xFF881337L,
            badgeIconName = "palette",
            descriptionEn = "Perceives subtle patterns, styles, and brushstrokes.",
            descriptionFa = "درک‌کننده ظریف‌ترین الگوها و خطوط پنهان."
        ),
        AvatarDefinition(
            id = "scientist",
            nameEn = "Scientist",
            nameFa = "دانشمند",
            emoji = "🔬",
            primaryColor = 0xFF10B981L,
            secondaryColor = 0xFF064E3BL,
            badgeIconName = "biotech",
            descriptionEn = "Forensic analyst testing hypotheses rigorously.",
            descriptionFa = "آزمایشگر فرضیه‌ها با روش‌های علمی استوار."
        ),
        AvatarDefinition(
            id = "pirate",
            nameEn = "Pirate",
            nameFa = "دزد دریایی",
            emoji = "🏴‍☠️",
            primaryColor = 0xFFB45309L,
            secondaryColor = 0xFF451A03L,
            badgeIconName = "sailing",
            descriptionEn = "Fearless navigator pursuing hidden treasures.",
            descriptionFa = "دریانورد شجاع در جستجوی گنجینه‌های مدفون."
        ),
        AvatarDefinition(
            id = "knight",
            nameEn = "Knight",
            nameFa = "شوالیه",
            emoji = "🛡️",
            primaryColor = 0xFF64748BL,
            secondaryColor = 0xFF1E293BL,
            badgeIconName = "shield",
            descriptionEn = "Steadfast defender of truth and honor.",
            descriptionFa = "مدافع استوار حقیقت، سوگند و شرافت."
        ),
        AvatarDefinition(
            id = "alien",
            nameEn = "Alien",
            nameFa = "بیگانه",
            emoji = "👽",
            primaryColor = 0xFF84CC16L,
            secondaryColor = 0xFF365314L,
            badgeIconName = "public",
            descriptionEn = "Possesses strange intuition from distant worlds.",
            descriptionFa = "دارای شهود غریب و ناشناخته از دنیاهای دوردست."
        ),
        AvatarDefinition(
            id = "penguin",
            nameEn = "Penguin",
            nameFa = "پنگوئن",
            emoji = "🐧",
            primaryColor = 0xFF0284C7L,
            secondaryColor = 0xFF082F49L,
            badgeIconName = "ac_unit",
            descriptionEn = "Cool, calm, and collected under cross-examination.",
            descriptionFa = "خونسرد و مسلط در برابر سخت‌ترین بازجویی‌ها."
        ),
        AvatarDefinition(
            id = "raven",
            nameEn = "Raven",
            nameFa = "کلاغ",
            emoji = "🪶",
            primaryColor = 0xFF475569L,
            secondaryColor = 0xFF0F172AL,
            badgeIconName = "feather",
            descriptionEn = "Harbinger of cryptic omens and lost knowledge.",
            descriptionFa = "پیام‌آور نشانه‌های مبهم و دانش‌های گم‌شده."
        ),
        AvatarDefinition(
            id = "explorer",
            nameEn = "Explorer",
            nameFa = "کاوشگر",
            emoji = "🧭",
            primaryColor = 0xFFD97706L,
            secondaryColor = 0xFF78350FL,
            badgeIconName = "explore",
            descriptionEn = "Pathfinder untangling labyrinthine conspiracies.",
            descriptionFa = "راه‌یاب در هزارتوهای معما و توطئه‌های پیچیده."
        ),
        AvatarDefinition(
            id = "jester",
            nameEn = "Jester",
            nameFa = "دلقک",
            emoji = "🃏",
            primaryColor = 0xFFD946EFL,
            secondaryColor = 0xFF701A75L,
            badgeIconName = "theater_comedy",
            descriptionEn = "Feigns foolishness to extract guarded confessions.",
            descriptionFa = "با نقاب بازیگوشی، اعتراف‌های مهم را آشکار می‌کند."
        ),
        AvatarDefinition(
            id = "ninja",
            nameEn = "Ninja",
            nameFa = "نینجا",
            emoji = "🥷",
            primaryColor = 0xFF334155L,
            secondaryColor = 0xFF020617L,
            badgeIconName = "visibility_off",
            descriptionEn = "Shadow operative extracting intelligence unseen.",
            descriptionFa = "عامل سایه‌ها که اطلاعات را نامحسوس جمع‌آوری می‌کند."
        ),
        AvatarDefinition(
            id = "wizard",
            nameEn = "Wizard",
            nameFa = "جادوگر",
            emoji = "🧙",
            primaryColor = 0xFF7C3AEDL,
            secondaryColor = 0xFF2E1065L,
            badgeIconName = "flare",
            descriptionEn = "Master of ancient runic riddles and arcane logic.",
            descriptionFa = "استاد معماهای کهن و منطق‌های شگفت‌انگیز."
        ),
        AvatarDefinition(
            id = "inventor",
            nameEn = "Inventor",
            nameFa = "مخترع",
            emoji = "⚙️",
            primaryColor = 0xFFEAB308L,
            secondaryColor = 0xFF713F12L,
            badgeIconName = "build",
            descriptionEn = "Constructs ingenious traps and evidence decoders.",
            descriptionFa = "سازنده ابزارهای نبوغ‌آمیز برای رمزگشایی مدارک."
        ),
        AvatarDefinition(
            id = "wolf",
            nameEn = "Wolf",
            nameFa = "گرگ",
            emoji = "🐺",
            primaryColor = 0xFF6B7280L,
            secondaryColor = 0xFF1F2937L,
            badgeIconName = "crisis_alert",
            descriptionEn = "Tenacious tracker hunting down elusive trails.",
            descriptionFa = "ردیاب سرسخت ردپاهای فرار و پنهان‌کاری."
        ),
        AvatarDefinition(
            id = "panda",
            nameEn = "Panda",
            nameFa = "پاندا",
            emoji = "🐼",
            primaryColor = 0xFF14B8A6L,
            secondaryColor = 0xFF134E4AL,
            badgeIconName = "spa",
            descriptionEn = "Zen detective cutting through distractions.",
            descriptionFa = "کارآگاه صبور و آرام با تمرکز روی اصل ماجرا."
        ),
        AvatarDefinition(
            id = "time_traveler",
            nameEn = "Time Traveler",
            nameFa = "مسافر زمان",
            emoji = "⏳",
            primaryColor = 0xFF3B82F6L,
            secondaryColor = 0xFF1E3A8AL,
            badgeIconName = "hourglass_top",
            descriptionEn = "Reconstructs timelines with chronological precision.",
            descriptionFa = "بازساز خطوط زمانی حوادث با دقت بی‌نظیر."
        ),
        AvatarDefinition(
            id = "phantom",
            nameEn = "Phantom",
            nameFa = "فانتوم",
            emoji = "🎭",
            primaryColor = 0xFF4F46E5L,
            secondaryColor = 0xFF1E1B4BL,
            badgeIconName = "masks",
            descriptionEn = "Enigmatic mastermind always one step ahead.",
            descriptionFa = "مغز متفکر رازآلود که همیشه یک گام جلوتر است."
        )
    )

    fun findById(id: String): AvatarDefinition {
        return ALL_AVATARS.find { it.id == id } ?: ALL_AVATARS.first()
    }
}
