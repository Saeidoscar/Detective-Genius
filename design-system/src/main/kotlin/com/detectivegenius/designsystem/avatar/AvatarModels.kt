package com.detectivegenius.designsystem.avatar

import androidx.compose.ui.graphics.Color

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
    val primaryColor: Color,
    val secondaryColor: Color,
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
            primaryColor = Color(0xFF6366F1),
            secondaryColor = Color(0xFF312E81),
            badgeIconName = "search",
            descriptionEn = "Master of observation and inductive reasoning.",
            descriptionFa = "استاد مشاهده دقیق و استنتاج‌های منطقی."
        ),
        AvatarDefinition(
            id = "magician",
            nameEn = "Magician",
            nameFa = "شعبده‌باز",
            emoji = "🎩",
            primaryColor = Color(0xFF8B5CF6),
            secondaryColor = Color(0xFF4C1D95),
            badgeIconName = "auto_fix_high",
            descriptionEn = "Keeper of secrets, sleights, and misdirection.",
            descriptionFa = "نگهبان رازها و شعبده‌های غیرقابل پیش‌بینی."
        ),
        AvatarDefinition(
            id = "astronaut",
            nameEn = "Astronaut",
            nameFa = "فضانورد",
            emoji = "🧑‍🚀",
            primaryColor = Color(0xFF0EA5E9),
            secondaryColor = Color(0xFF075985),
            badgeIconName = "rocket_launch",
            descriptionEn = "Explorer of uncharted cosmic mysteries.",
            descriptionFa = "کاوشگر ناشناخته‌ها و رازهای کهکشانی."
        ),
        AvatarDefinition(
            id = "fox",
            nameEn = "Fox",
            nameFa = "روباه",
            emoji = "🦊",
            primaryColor = Color(0xFFF97316),
            secondaryColor = Color(0xFF9A3412),
            badgeIconName = "pets",
            descriptionEn = "Cunning investigator who reads between the lines.",
            descriptionFa = "زیرک و چابک در خواندن نیت‌های پنهان."
        ),
        AvatarDefinition(
            id = "owl",
            nameEn = "Owl",
            nameFa = "جغد",
            emoji = "🦉",
            primaryColor = Color(0xFFA855F7),
            secondaryColor = Color(0xFF581C87),
            badgeIconName = "visibility",
            descriptionEn = "Wise night watcher with unmatched scrutiny.",
            descriptionFa = "دیده‌بان خردمند شب با نگاهی ژرف‌نگر."
        ),
        AvatarDefinition(
            id = "cat",
            nameEn = "Cat",
            nameFa = "گربه",
            emoji = "🐱",
            primaryColor = Color(0xFFEC4899),
            secondaryColor = Color(0xFF831843),
            badgeIconName = "cruelty_free",
            descriptionEn = "Silent inquisitor slipping through locked doors.",
            descriptionFa = "کارآگاهی آرام که از تمام درهای بسته عبور می‌کند."
        ),
        AvatarDefinition(
            id = "robot",
            nameEn = "Robot",
            nameFa = "ربات",
            emoji = "🤖",
            primaryColor = Color(0xFF06B6D4),
            secondaryColor = Color(0xFF164E63),
            badgeIconName = "memory",
            descriptionEn = "High-precision computational deduction unit.",
            descriptionFa = "واحد پردازش تحلیلی با محاسبات فوق دقیق."
        ),
        AvatarDefinition(
            id = "ghost",
            nameEn = "Ghost",
            nameFa = "شبح",
            emoji = "👻",
            primaryColor = Color(0xFF94A3B8),
            secondaryColor = Color(0xFF334155),
            badgeIconName = "blur_on",
            descriptionEn = "Unseen presence listening to confidential whispers.",
            descriptionFa = "حضور نادیدنی که زمزمه‌های محرمانه را می‌شنود."
        ),
        AvatarDefinition(
            id = "artist",
            nameEn = "Artist",
            nameFa = "هنرمند",
            emoji = "🎨",
            primaryColor = Color(0xFFF43F5E),
            secondaryColor = Color(0xFF881337),
            badgeIconName = "palette",
            descriptionEn = "Perceives subtle patterns, styles, and brushstrokes.",
            descriptionFa = "درک‌کننده ظریف‌ترین الگوها و خطوط پنهان."
        ),
        AvatarDefinition(
            id = "scientist",
            nameEn = "Scientist",
            nameFa = "دانشمند",
            emoji = "🔬",
            primaryColor = Color(0xFF10B981),
            secondaryColor = Color(0xFF064E3B),
            badgeIconName = "biotech",
            descriptionEn = "Forensic analyst testing hypotheses rigorously.",
            descriptionFa = "آزمایشگر فرضیه‌ها با روش‌های علمی استوار."
        ),
        AvatarDefinition(
            id = "pirate",
            nameEn = "Pirate",
            nameFa = "دزد دریایی",
            emoji = "🏴‍☠️",
            primaryColor = Color(0xFFB45309),
            secondaryColor = Color(0xFF451A03),
            badgeIconName = "sailing",
            descriptionEn = "Fearless navigator pursuing hidden treasures.",
            descriptionFa = "دریانورد شجاع در جستجوی گنجینه‌های مدفون."
        ),
        AvatarDefinition(
            id = "knight",
            nameEn = "Knight",
            nameFa = "شوالیه",
            emoji = "🛡️",
            primaryColor = Color(0xFF64748B),
            secondaryColor = Color(0xFF1E293B),
            badgeIconName = "shield",
            descriptionEn = "Steadfast defender of truth and honor.",
            descriptionFa = "مدافع استوار حقیقت، سوگند و شرافت."
        ),
        AvatarDefinition(
            id = "alien",
            nameEn = "Alien",
            nameFa = "بیگانه",
            emoji = "👽",
            primaryColor = Color(0xFF84CC16),
            secondaryColor = Color(0xFF365314),
            badgeIconName = "public",
            descriptionEn = "Possesses strange intuition from distant worlds.",
            descriptionFa = "دارای شهود غریب و ناشناخته از دنیاهای دوردست."
        ),
        AvatarDefinition(
            id = "penguin",
            nameEn = "Penguin",
            nameFa = "پنگوئن",
            emoji = "🐧",
            primaryColor = Color(0xFF0284C7),
            secondaryColor = Color(0xFF082F49),
            badgeIconName = "ac_unit",
            descriptionEn = "Cool, calm, and collected under cross-examination.",
            descriptionFa = "خونسرد و مسلط در برابر سخت‌ترین بازجویی‌ها."
        ),
        AvatarDefinition(
            id = "raven",
            nameEn = "Raven",
            nameFa = "کلاغ",
            emoji = "🪶",
            primaryColor = Color(0xFF475569),
            secondaryColor = Color(0xFF0F172A),
            badgeIconName = "feather",
            descriptionEn = "Harbinger of cryptic omens and lost knowledge.",
            descriptionFa = "پیام‌آور نشانه‌های مبهم و دانش‌های گم‌شده."
        ),
        AvatarDefinition(
            id = "explorer",
            nameEn = "Explorer",
            nameFa = "کاوشگر",
            emoji = "🧭",
            primaryColor = Color(0xFFD97706),
            secondaryColor = Color(0xFF78350F),
            badgeIconName = "explore",
            descriptionEn = "Pathfinder untangling labyrinthine conspiracies.",
            descriptionFa = "راه‌یاب در هزارتوهای معما و توطئه‌های پیچیده."
        ),
        AvatarDefinition(
            id = "jester",
            nameEn = "Jester",
            nameFa = "دلقک",
            emoji = "🃏",
            primaryColor = Color(0xFFD946EF),
            secondaryColor = Color(0xFF701A75),
            badgeIconName = "theater_comedy",
            descriptionEn = "Feigns foolishness to extract guarded confessions.",
            descriptionFa = "با نقاب بازیگوشی، اعتراف‌های مهم را آشکار می‌کند."
        ),
        AvatarDefinition(
            id = "ninja",
            nameEn = "Ninja",
            nameFa = "نینجا",
            emoji = "🥷",
            primaryColor = Color(0xFF334155),
            secondaryColor = Color(0xFF020617),
            badgeIconName = "visibility_off",
            descriptionEn = "Shadow operative extracting intelligence unseen.",
            descriptionFa = "عامل سایه‌ها که اطلاعات را نامحسوس جمع‌آوری می‌کند."
        ),
        AvatarDefinition(
            id = "wizard",
            nameEn = "Wizard",
            nameFa = "جادوگر",
            emoji = "🧙",
            primaryColor = Color(0xFF7C3AED),
            secondaryColor = Color(0xFF2E1065),
            badgeIconName = "flare",
            descriptionEn = "Master of ancient runic riddles and arcane logic.",
            descriptionFa = "استاد معماهای کهن و منطق‌های شگفت‌انگیز."
        ),
        AvatarDefinition(
            id = "inventor",
            nameEn = "Inventor",
            nameFa = "مخترع",
            emoji = "⚙️",
            primaryColor = Color(0xFFEAB308),
            secondaryColor = Color(0xFF713F12),
            badgeIconName = "build",
            descriptionEn = "Constructs ingenious traps and evidence decoders.",
            descriptionFa = "سازنده ابزارهای نبوغ‌آمیز برای رمزگشایی مدارک."
        ),
        AvatarDefinition(
            id = "wolf",
            nameEn = "Wolf",
            nameFa = "گرگ",
            emoji = "🐺",
            primaryColor = Color(0xFF6B7280),
            secondaryColor = Color(0xFF1F2937),
            badgeIconName = "crisis_alert",
            descriptionEn = "Tenacious tracker hunting down elusive trails.",
            descriptionFa = "ردیاب سرسخت ردپاهای فرار و پنهان‌کاری."
        ),
        AvatarDefinition(
            id = "panda",
            nameEn = "Panda",
            nameFa = "پاندا",
            emoji = "🐼",
            primaryColor = Color(0xFF14B8A6),
            secondaryColor = Color(0xFF134E4A),
            badgeIconName = "spa",
            descriptionEn = "Zen detective cutting through distractions.",
            descriptionFa = "کارآگاه صبور و آرام با تمرکز روی اصل ماجرا."
        ),
        AvatarDefinition(
            id = "time_traveler",
            nameEn = "Time Traveler",
            nameFa = "مسافر زمان",
            emoji = "⏳",
            primaryColor = Color(0xFF3B82F6),
            secondaryColor = Color(0xFF1E3A8A),
            badgeIconName = "hourglass_top",
            descriptionEn = "Reconstructs timelines with chronological precision.",
            descriptionFa = "بازساز خطوط زمانی حوادث با دقت بی‌نظیر."
        ),
        AvatarDefinition(
            id = "phantom",
            nameEn = "Phantom",
            nameFa = "فانتوم",
            emoji = "🎭",
            primaryColor = Color(0xFF4F46E5),
            secondaryColor = Color(0xFF1E1B4B),
            badgeIconName = "masks",
            descriptionEn = "Enigmatic mastermind always one step ahead.",
            descriptionFa = "مغز متفکر رازآلود که همیشه یک گام جلوتر است."
        )
    )

    fun findById(id: String): AvatarDefinition {
        return ALL_AVATARS.find { it.id == id } ?: ALL_AVATARS.first()
    }
}
