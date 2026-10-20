package com.omra.guide

import android.app.Activity
import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.Gravity
import android.view.View
import android.view.ViewGroup.LayoutParams as ViewGroupLp
import android.view.WindowManager
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.ScrollView
import android.widget.TextView

/**
 * Mon Omra / عمرتي : guide pas à pas des rites de la Omra, bilingue
 * français / arabe, avec invocations sourcées et compteurs de Tawaf et de Sa'i.
 * Fonctionne entièrement hors ligne.
 */
class MainActivity : Activity() {

    // ------------------------------------------------------------------
    // Contenu
    // ------------------------------------------------------------------
    private data class Dua(
        val titreFr: String,
        val titreAr: String,
        val arabe: String,
        val translit: String = "",
        val sensFr: String = "",
        val noteFr: String = "",
        val noteAr: String = "",
        val sourceFr: String = "",
        val sourceAr: String = ""
    )

    private data class Etape(
        val emoji: String,
        val titreFr: String,
        val titreAr: String,
        val resumeFr: String,
        val resumeAr: String,
        val consignesFr: List<String>,
        val consignesAr: List<String>,
        val duas: List<Dua>,
        val compteur: Int = 0 // 0 = aucun, 1 = Tawaf, 2 = Sa'i
    )

    /** Un repère à reconnaître sur place, avec sa photo. */
    private data class Repere(
        val image: String,
        val titreFr: String,
        val titreAr: String,
        val texteFr: String,
        val texteAr: String
    )

    /** Un lieu à ouvrir dans Google Maps. */
    private data class Lieu(
        val nomFr: String,
        val nomAr: String,
        val requete: String,
        val noteFr: String = "",
        val noteAr: String = ""
    )

    /** Photo, auteur et licence (Wikimedia Commons) pour l'écran des crédits. */
    private data class Credit(val image: String, val titre: String, val auteur: String, val licence: String)

    private val talbiya = Dua(
        "Talbiya", "التلبية",
        "لَبَّيْكَ اللَّهُمَّ لَبَّيْكَ، لَبَّيْكَ لَا شَرِيكَ لَكَ لَبَّيْكَ، إِنَّ الْحَمْدَ وَالنِّعْمَةَ لَكَ وَالْمُلْكَ، لَا شَرِيكَ لَكَ",
        "Labbayka Allâhumma labbayk, labbayka lâ sharîka laka labbayk, inna l-hamda wa n-ni'mata laka wa l-mulk, lâ sharîka lak.",
        "Me voici, ô Allah, me voici. Me voici, Tu n'as pas d'associé, me voici. La louange, le bienfait et la royauté T'appartiennent. Tu n'as pas d'associé.",
        "À répéter souvent depuis le Miqat. Hommes à voix haute, femmes à voix basse. On l'arrête au début du Tawaf (avis majoritaire) ; selon l'imam Mâlik, celui qui a pris l'Ihram au Miqat l'arrête en arrivant au Haram.",
        "يُكثر منها من الميقات، يرفع بها الرجال أصواتهم وتخفضها النساء. وتُقطع عند بدء الطواف عند الجمهور، وعند الإمام مالك: من أحرم من الميقات قطعها إذا انتهى إلى الحرم.",
        "Bukhari et Muslim", "البخاري ومسلم"
    )

    private val etapes = listOf(
        Etape(
            "🤍", "L'Ihram", "الإحرام",
            "Se préparer et entrer en état de sacralisation", "الاستعداد والدخول في النسك",
            listOf(
                "Avant le Miqat : faire le ghusl (grand lavage), couper ongles et poils si besoin.",
                "Hommes : parfum sur le corps (pas sur les vêtements), puis deux pièces blanches non cousues (izar et rida) et des sandales.",
                "Femmes : vêtements habituels couvrants, sans niqab, sans gants et sans parfum.",
                "Au Miqat (en avion : à l'annonce du passage du Miqat), formulez l'intention de la Omra.",
                "Commencez la Talbiya et répétez-la souvent."
            ),
            listOf(
                "قبل الميقات: الاغتسال، وتقليم الأظافر وإزالة الشعر عند الحاجة.",
                "الرجال: التطيب في البدن لا في ثياب الإحرام، ثم لبس إزار ورداء أبيضين غير مخيطين، ونعلين.",
                "النساء: لباس ساتر معتاد، بلا نقاب ولا قفازين ولا طيب.",
                "عند الميقات (وفي الطائرة عند الإعلان عن محاذاته) انوِ العمرة.",
                "ابدأ التلبية وأكثر منها."
            ),
            listOf(
                Dua(
                    "Intention (au Miqat)", "نية العمرة (عند الميقات)",
                    "لَبَّيْكَ عُمْرَةً", "Labbayka 'umratan.",
                    "Me voici, (répondant à Ton appel) pour une Omra.",
                    sourceFr = "Muslim", sourceAr = "مسلم"
                ),
                Dua(
                    "Condition (facultative)", "الاشتراط (اختياري)",
                    "اللَّهُمَّ مَحِلِّي حَيْثُ حَبَسْتَنِي", "Allâhumma mahillî haythu habastanî.",
                    "Ô Allah, je me désacralise là où Tu m'empêches de continuer.",
                    "Pour celui qui craint un empêchement (maladie, etc.).",
                    "لمن خاف عائقًا يمنعه من الإتمام كالمرض.",
                    "Bukhari et Muslim", "البخاري ومسلم"
                ),
                talbiya
            )
        ),
        Etape(
            "⛔", "Les interdits de l'Ihram", "محظورات الإحرام",
            "Ce qu'il faut éviter jusqu'à la fin", "ما يجب اجتنابه حتى التحلل",
            listOf(
                "Couper ou arracher cheveux, poils ou ongles.",
                "Se parfumer : corps, vêtements, savon ou crème parfumés.",
                "Hommes : se couvrir la tête (bonnet, casquette) ou porter des vêtements cousus ajustés (chemise, pantalon, chaussettes).",
                "Femmes : porter le niqab ou des gants. Se couvrir le visage avec un voile tombant devant des hommes étrangers reste permis.",
                "Chasser un animal terrestre.",
                "Conclure un mariage ou faire une demande en mariage.",
                "Relations intimes et préliminaires.",
                "Éviter les disputes, les grossièretés et les péchés."
            ),
            listOf(
                "قص الشعر أو نتفه، وتقليم الأظافر.",
                "التطيب في البدن أو الثوب، أو استعمال صابون أو كريم معطر.",
                "الرجال: تغطية الرأس بملاصق (طاقية أو قبعة)، أو لبس المخيط المفصّل على البدن (قميص، سروال، جوارب).",
                "النساء: لبس النقاب أو القفازين، ويجوز لها إسدال الخمار على وجهها أمام الرجال الأجانب.",
                "صيد الحيوان البري.",
                "عقد النكاح أو الخِطبة.",
                "الجماع ومقدماته.",
                "اجتناب الجدال والفسوق والمعاصي."
            ),
            listOf(talbiya)
        ),
        Etape(
            "🕌", "Entrée à la Mosquée al-Haram", "دخول المسجد الحرام",
            "Entrer et se préparer au Tawaf", "الدخول والاستعداد للطواف",
            listOf(
                "Entrez du pied droit en disant l'invocation d'entrée.",
                "Soyez en état d'ablutions (wudû') : elles sont exigées pour le Tawaf.",
                "Hommes : idtibâ' (épaule droite découverte) pendant le Tawaf, sunna selon la majorité. L'imam Mâlik ne la considérait pas comme une sunna.",
                "Arrêtez la Talbiya au début du Tawaf (selon l'imam Mâlik : dès l'arrivée au Haram)."
            ),
            listOf(
                "ادخل برجلك اليمنى وقل دعاء دخول المسجد.",
                "كن على وضوء، فهو شرط لصحة الطواف.",
                "الرجال: الاضطباع (كشف الكتف الأيمن) في الطواف سنة عند الجمهور، ولم يره الإمام مالك سنة.",
                "اقطع التلبية عند بدء الطواف (وعند الإمام مالك: عند الوصول إلى الحرم)."
            ),
            listOf(
                Dua(
                    "Entrée à la mosquée (pied droit)", "دعاء دخول المسجد (بالرجل اليمنى)",
                    "بِسْمِ اللهِ، وَالصَّلَاةُ وَالسَّلَامُ عَلَى رَسُولِ اللهِ، اللَّهُمَّ افْتَحْ لِي أَبْوَابَ رَحْمَتِكَ",
                    "Bismillâh, wa s-salâtu wa s-salâmu 'alâ rasûlillâh. Allâhumma ftah lî abwâba rahmatik.",
                    "Au nom d'Allah, que la prière et la paix soient sur le Messager d'Allah. Ô Allah, ouvre-moi les portes de Ta miséricorde.",
                    sourceFr = "Muslim, Ibn Mâja", sourceAr = "مسلم، ابن ماجه"
                ),
                Dua(
                    "Protection en entrant", "الاستعاذة عند الدخول",
                    "أَعُوذُ بِاللهِ الْعَظِيمِ، وَبِوَجْهِهِ الْكَرِيمِ، وَسُلْطَانِهِ الْقَدِيمِ، مِنَ الشَّيْطَانِ الرَّجِيمِ",
                    "A'ûdhu billâhi l-'azîm, wa bi-wajhihi l-karîm, wa sultânihi l-qadîm, mina sh-shaytâni r-rajîm.",
                    "Je cherche refuge auprès d'Allah l'Immense, auprès de Son noble Visage et de Son autorité éternelle, contre Satan le maudit.",
                    sourceFr = "Abû Dâwûd", sourceAr = "أبو داود"
                )
            )
        ),
        Etape(
            "🕋", "Le Tawaf", "الطواف",
            "7 tours autour de la Kaaba", "سبعة أشواط حول الكعبة",
            listOf(
                "Commencez à la ligne de la Pierre noire, la Kaaba à votre gauche.",
                "Tournez à l'extérieur du Hijr Ismâ'il (le muret en demi-cercle) : un tour fait à l'intérieur n'est pas valable.",
                "7 tours complets : chaque tour se termine en revenant à la Pierre noire.",
                "Hommes : raml (petits pas rapides) pendant les 3 premiers tours, si possible.",
                "Touchez le coin yéménite de la main si c'est facile, sans le baiser ni faire de signe.",
                "En cas de doute sur le nombre de tours pendant le Tawaf, retenez le plus petit nombre.",
                "Appuyez sur le bouton à la fin de chaque tour."
            ),
            listOf(
                "ابدأ من محاذاة الحجر الأسود، واجعل الكعبة عن يسارك.",
                "طُف من خارج حِجر إسماعيل (الحطيم)، فالشوط من داخله لا يصح.",
                "سبعة أشواط كاملة، ينتهي كل شوط عند الحجر الأسود.",
                "الرجال: الرَّمَل في الأشواط الثلاثة الأولى إن تيسر.",
                "استلم الركن اليماني بيدك إن تيسر، دون تقبيل ولا إشارة.",
                "إذا شككت في عدد الأشواط أثناء الطواف فابنِ على الأقل.",
                "اضغط على الزر عند نهاية كل شوط."
            ),
            listOf(
                Dua(
                    "À la Pierre noire (début de chaque tour)", "عند الحجر الأسود (بداية كل شوط)",
                    "بِسْمِ اللهِ، وَاللهُ أَكْبَرُ", "Bismillâh, wa Llâhu akbar.",
                    "Au nom d'Allah, et Allah est le plus grand.",
                    "Embrassez la Pierre si c'est facile ; sinon touchez-la de la main et embrassez votre main ; sinon faites un signe de la main droite sans l'embrasser. Aux tours suivants, « Allâhu akbar » suffit.",
                    "قبّل الحجر إن تيسر، وإلا فاستلمه بيدك وقبّل يدك، وإلا فأشر إليه بيدك اليمنى دون تقبيل. ويكفي «الله أكبر» في بقية الأشواط.",
                    "Takbir : Bukhari ; basmala : d'Ibn 'Umar (Bayhaqi)", "التكبير: البخاري، والتسمية عن ابن عمر (البيهقي)"
                ),
                Dua(
                    "Entre le coin yéménite et la Pierre noire", "بين الركن اليماني والحجر الأسود",
                    "رَبَّنَا آتِنَا فِي الدُّنْيَا حَسَنَةً وَفِي الْآخِرَةِ حَسَنَةً وَقِنَا عَذَابَ النَّارِ",
                    "Rabbanâ âtinâ fi d-dunyâ hasanatan wa fi l-âkhirati hasanatan wa qinâ 'adhâba n-nâr.",
                    "Seigneur, accorde-nous une belle part ici-bas, une belle part dans l'au-delà, et protège-nous du châtiment du Feu.",
                    sourceFr = "Coran 2:201 ; Abû Dâwûd", sourceAr = "البقرة 201، أبو داود"
                ),
                Dua(
                    "Pendant le reste du tour", "في بقية الشوط", "",
                    noteFr = "Aucune invocation précise n'est fixée pour chaque tour : invoquez librement, dans votre langue, faites du dhikr ou lisez le Coran.",
                    noteAr = "لا يوجد دعاء مخصوص لكل شوط؛ ادعُ بما شئت وبأي لغة، واذكر الله أو اقرأ القرآن."
                )
            ),
            compteur = 1
        ),
        Etape(
            "🤲", "Prière au Maqam Ibrahim", "الصلاة خلف مقام إبراهيم",
            "2 rak'at après le Tawaf", "ركعتان بعد الطواف",
            listOf(
                "Hommes : couvrez de nouveau l'épaule droite.",
                "Priez 2 rak'at derrière le Maqam Ibrahim si possible, sinon n'importe où dans la mosquée.",
                "Ne gênez pas les personnes qui font le Tawaf."
            ),
            listOf(
                "الرجال: غطِّ كتفك الأيمن.",
                "صلِّ ركعتين خلف مقام إبراهيم إن تيسر، وإلا ففي أي مكان من المسجد.",
                "لا تؤذِ الطائفين."
            ),
            listOf(
                Dua(
                    "En allant vers le Maqam Ibrahim", "عند التوجه إلى المقام",
                    "وَاتَّخِذُوا مِنْ مَقَامِ إِبْرَاهِيمَ مُصَلًّى", "Wattakhidhû min maqâmi Ibrâhîma musallâ.",
                    "Et prenez la station d'Ibrahim pour lieu de prière.",
                    sourceFr = "Coran 2:125 ; Muslim", sourceAr = "البقرة 125، مسلم"
                ),
                Dua(
                    "Les 2 rak'at", "الركعتان", "",
                    noteFr = "1ʳᵉ rak'a : al-Fâtiha puis sourate al-Kâfirûn (109).\n2ᵉ rak'a : al-Fâtiha puis sourate al-Ikhlâs (112).",
                    noteAr = "في الأولى: الفاتحة و«قل يا أيها الكافرون».\nفي الثانية: الفاتحة و«قل هو الله أحد».",
                    sourceFr = "Muslim", sourceAr = "مسلم"
                )
            )
        ),
        Etape(
            "💧", "L'eau de Zamzam", "ماء زمزم",
            "Boire et invoquer", "الشرب والدعاء",
            listOf(
                "Buvez de l'eau de Zamzam en invoquant Allah pour ce que vous souhaitez.",
                "Si c'est facile, retournez toucher la Pierre noire avant le Sa'i.",
                "Dirigez-vous ensuite vers le mont Safa."
            ),
            listOf(
                "اشرب من ماء زمزم وادعُ بما شئت.",
                "إن تيسر فارجع واستلم الحجر الأسود قبل السعي.",
                "ثم اتجه إلى الصفا."
            ),
            listOf(
                Dua(
                    "En buvant Zamzam", "عند شرب زمزم",
                    "مَاءُ زَمْزَمَ لِمَا شُرِبَ لَهُ", "Mâ'u Zamzama limâ shuriba lah.",
                    "L'eau de Zamzam est pour ce pour quoi on la boit.",
                    "Faites les invocations de votre choix en la buvant.",
                    "ادعُ بما شئت عند شربه.",
                    "Ibn Mâja, Ahmad (jugé sahih par al-Albânî)", "ابن ماجه وأحمد (صححه الألباني)"
                )
            )
        ),
        Etape(
            "⛰️", "Le Sa'i", "السعي",
            "7 trajets entre Safa et Marwa", "سبعة أشواط بين الصفا والمروة",
            listOf(
                "7 trajets : Safa → Marwa = 1, Marwa → Safa = 2… On termine à Marwa.",
                "Sur Safa et sur Marwa, faites face à la Kaaba, levez les mains et invoquez.",
                "Hommes : trottinez entre les deux lumières vertes.",
                "Les ablutions sont recommandées mais pas obligatoires pour le Sa'i.",
                "Ne coupez pas vos cheveux avant d'avoir terminé le Sa'i.",
                "Appuyez sur le bouton à chaque arrivée sur une colline."
            ),
            listOf(
                "سبعة أشواط: من الصفا إلى المروة شوط، ومن المروة إلى الصفا شوط ثانٍ… وتنتهي عند المروة.",
                "على الصفا والمروة: استقبل الكعبة وارفع يديك وادعُ.",
                "الرجال: الإسراع بين العلمين الأخضرين.",
                "الوضوء للسعي مستحب وليس شرطًا.",
                "لا تحلق ولا تقصّر قبل إتمام السعي.",
                "اضغط على الزر عند الوصول إلى كل جبل."
            ),
            listOf(
                Dua(
                    "En approchant de Safa (1ʳᵉ fois seulement)", "عند الدنو من الصفا (أول مرة فقط)",
                    "إِنَّ الصَّفَا وَالْمَرْوَةَ مِنْ شَعَائِرِ اللَّهِ\nأَبْدَأُ بِمَا بَدَأَ اللَّهُ بِهِ",
                    "Inna s-Safâ wa l-Marwata min sha'â'iri Llâh.\nAbda'u bimâ bada'a Llâhu bih.",
                    "Safa et Marwa sont vraiment parmi les rites d'Allah.\nJe commence par ce par quoi Allah a commencé.",
                    sourceFr = "Coran 2:158 ; Muslim", sourceAr = "البقرة 158، مسلم"
                ),
                Dua(
                    "Sur Safa et sur Marwa (face à la Kaaba, mains levées)", "على الصفا والمروة (مستقبلًا الكعبة رافعًا يديك)",
                    "اللهُ أَكْبَرُ، اللهُ أَكْبَرُ، اللهُ أَكْبَرُ\nلَا إِلَهَ إِلَّا اللهُ وَحْدَهُ لَا شَرِيكَ لَهُ، لَهُ الْمُلْكُ وَلَهُ الْحَمْدُ، وَهُوَ عَلَى كُلِّ شَيْءٍ قَدِيرٌ، لَا إِلَهَ إِلَّا اللهُ وَحْدَهُ، أَنْجَزَ وَعْدَهُ، وَنَصَرَ عَبْدَهُ، وَهَزَمَ الْأَحْزَابَ وَحْدَهُ",
                    "Allâhu akbar (×3). Lâ ilâha illa Llâhu wahdahu lâ sharîka lah, lahu l-mulku wa lahu l-hamd, wa huwa 'alâ kulli shay'in qadîr. Lâ ilâha illa Llâhu wahdah, anjaza wa'dah, wa nasara 'abdah, wa hazama l-ahzâba wahdah.",
                    "Allah est le plus grand (×3). Il n'y a de divinité qu'Allah, Seul, sans associé. À Lui la royauté et la louange, et Il est capable de toute chose. Il n'y a de divinité qu'Allah, Seul. Il a tenu Sa promesse, secouru Son serviteur et vaincu Seul les coalisés.",
                    "À dire 3 fois, avec des invocations personnelles entre chaque fois.",
                    "يقال ثلاث مرات، ويُدعى بين ذلك.",
                    "Muslim", "مسلم"
                ),
                Dua(
                    "Entre les lumières vertes", "بين العلمين الأخضرين",
                    "رَبِّ اغْفِرْ وَارْحَمْ، إِنَّكَ أَنْتَ الْأَعَزُّ الْأَكْرَمُ",
                    "Rabbi ghfir warham, innaka anta l-a'azzu l-akram.",
                    "Seigneur, pardonne et fais miséricorde, Tu es certes le Plus Puissant, le Plus Généreux.",
                    "Parole d'Ibn Mas'ûd ; son attribution au Prophète ﷺ est faible. Vous pouvez aussi invoquer librement.",
                    "ثابت عن ابن مسعود موقوفًا، ورفعه إلى النبي ﷺ ضعيف. ويجوز الدعاء بغيره.",
                    "Tabarânî", "الطبراني"
                )
            ),
            compteur = 2
        ),
        Etape(
            "✂️", "Rasage ou raccourcissement", "الحلق أو التقصير",
            "Fin de la Omra", "التحلل من العمرة",
            listOf(
                "Hommes : se raser la tête (le meilleur) ou raccourcir les cheveux sur toute la tête, pas seulement quelques mèches.",
                "Femmes : couper l'équivalent d'un bout de doigt (environ 2 cm) à l'extrémité des cheveux.",
                "Votre Omra est terminée : les interdits de l'Ihram sont levés."
            ),
            listOf(
                "الرجال: حلق الرأس (وهو الأفضل) أو تقصير الشعر من جميع الرأس، لا من بعض الخصلات فقط.",
                "النساء: قص قدر أنملة من أطراف الشعر.",
                "تمت عمرتك، وحلّ لك ما كان محظورًا بالإحرام."
            ),
            listOf(
                Dua(
                    "Pour féliciter", "للتهنئة",
                    "تَقَبَّلَ اللهُ مِنَّا وَمِنْكُمْ", "Taqabbala Llâhu minnâ wa minkum.",
                    "Qu'Allah accepte de nous et de vous."
                )
            )
        )
    )

    // ------------------------------------------------------------------
    // Photos, repères et lieux de chaque étape (même ordre que `etapes`)
    // ------------------------------------------------------------------
    private val imagesEtapes = listOf(
        "img_ihram", "", "img_mosquee", "img_tawaf", "img_maqam", "img_zamzam", "img_sai", ""
    )

    private val reperesEtapes: Map<Int, List<Repere>> = mapOf(
        3 to listOf(
            Repere(
                "img_pierre", "La Pierre noire", "الحجر الأسود",
                "Dans l'angle de la Kaaba, entourée d'un cadre argenté. Une lumière verte sur le mur de la mosquée indique la ligne de départ de chaque tour.",
                "في ركن الكعبة، داخل إطار فضي. ويدل ضوء أخضر على جدار المسجد على خط بداية كل شوط."
            ),
            Repere(
                "img_yamani", "Le coin yéménite", "الركن اليماني",
                "Le coin qui précède la Pierre noire. On le touche de la main si c'est facile, sans l'embrasser ni faire de signe.",
                "الركن الذي يسبق الحجر الأسود. يُستلم باليد إن تيسر، دون تقبيل ولا إشارة."
            ),
            Repere(
                "img_hijr", "Le Hijr Ismâ'il", "حِجر إسماعيل",
                "Le muret blanc en demi-cercle à côté de la Kaaba. Il fait partie de la Kaaba : tournez à l'extérieur, sinon le tour n'est pas valable.",
                "الجدار الأبيض نصف الدائري بجوار الكعبة، وهو من الكعبة؛ فطُف من خارجه وإلا لم يصح الشوط."
            )
        ),
        6 to listOf(
            Repere(
                "img_safa", "Le mont Safa", "جبل الصفا",
                "Point de départ du Sa'i, au sud de la galerie. Le rocher est visible sous le dôme.",
                "بداية السعي، في الطرف الجنوبي من المسعى، والصخرة ظاهرة تحت القبة."
            ),
            Repere(
                "img_marwa", "Le mont Marwa", "جبل المروة",
                "À l'autre extrémité de la galerie. Le 7ᵉ trajet se termine ici.",
                "في الطرف الآخر من المسعى، وعنده ينتهي الشوط السابع."
            )
        )
    )

    private val lieuxEtapes: Map<Int, List<Lieu>> = mapOf(
        0 to listOf(
            Lieu(
                "Miqat al-Juhfa (Rabigh)", "ميقات الجحفة (رابغ)", "Miqat Al Juhfah Mosque",
                "Pour les voyageurs venant du Maroc, d'Égypte et du Cham. En avion, l'Ihram se prend en passant au-dessus.",
                "لأهل المغرب ومصر والشام، ومن مرّ به. وفي الطائرة يُحرم عند محاذاته."
            ),
            Lieu("Dhul-Hulayfa (Abyar Ali)", "ذو الحليفة (أبيار علي)", "Dhul Hulaifah Miqat Mosque Abyar Ali",
                "Pour ceux qui viennent de Médine.", "لأهل المدينة ومن مرّ بها."),
            Lieu("Qarn al-Manazil (As-Sayl al-Kabir)", "قرن المنازل (السيل الكبير)", "Qarn Al Manazil Miqat Mosque",
                "Pour ceux qui viennent de Riyad, de Taïf et du Najd.", "لأهل نجد والطائف."),
            Lieu("Yalamlam", "يلملم", "Yalamlam Miqat Mosque",
                "Pour ceux qui viennent du Yémen et du sud.", "لأهل اليمن ومن جاء من الجنوب."),
            Lieu("Dhat Irq", "ذات عرق", "Dhat Irq Miqat",
                "Pour ceux qui viennent d'Irak et du nord-est.", "لأهل العراق."),
            Lieu("Masjid Aïcha (at-Tan'im)", "مسجد عائشة (التنعيم)", "Masjid Aisha Tan'eem Mecca",
                "Pour ceux qui sont déjà à La Mecque.", "لمن كان داخل مكة.")
        ),
        2 to listOf(
            Lieu("Masjid al-Haram", "المسجد الحرام", "Masjid al-Haram Mecca"),
            Lieu("Porte du Roi Abdelaziz", "باب الملك عبد العزيز", "King Abdulaziz Gate Masjid al-Haram")
        ),
        6 to listOf(
            Lieu("Mont Safa", "جبل الصفا", "Mount Safa Mecca"),
            Lieu("Mont Marwa", "جبل المروة", "Al-Marwah Mecca")
        ),
        7 to listOf(
            Lieu("Coiffeurs près de Marwa", "حلاقون قرب المروة", "barber near Al Marwah Mecca")
        )
    )

    private val credits = listOf(
        Credit("img_mosquee", "Masjid al-Haram", "Wurzelgnohm", "CC0"),
        Credit("img_ihram", "Ihram for umrah", "Rammaum", "CC BY-SA 3.0"),
        Credit("img_tawaf", "Tawaf of the Kaaba", "omar_chatriwala", "CC BY 2.0"),
        Credit("img_pierre", "The Blackstone", "Amerrycan Muslim", "CC BY-SA 3.0"),
        Credit("img_yamani", "Rukn al-Yamani", "Mohammad Hossein Velayati", "CC BY 4.0"),
        Credit("img_hijr", "Hajr Ismail, Makkah", "معتز توفيق اغبارية", "CC BY-SA 3.0"),
        Credit("img_maqam", "Maqam Ibrahim", "Ibrahim.ID", "CC BY-SA 3.0"),
        Credit("img_zamzam", "ماء زمزم في المسجد الحرام", "M sh شراونة محمد", "CC BY-SA 4.0"),
        Credit("img_sai", "Sa'y between Safa and Marwa", "Mohammad Hossein Velayati", "CC BY 4.0"),
        Credit("img_safa", "Al-Safa, Mecca", "Basamad a", "CC BY-SA 4.0"),
        Credit("img_marwa", "Al-Marwah", "Aiman titi", "CC BY-SA 3.0")
    )

    // ------------------------------------------------------------------
    // Couleurs
    // ------------------------------------------------------------------
    private val cFond = Color.parseColor("#F7F3E8")
    private val cVert = Color.parseColor("#14532D")
    private val cVertClair = Color.parseColor("#1F7A45")
    private val cOr = Color.parseColor("#B8891A")
    private val cTexte = Color.parseColor("#1C1C1C")
    private val cGris = Color.parseColor("#6B6B6B")
    private val cCarte = Color.WHITE

    // ------------------------------------------------------------------
    // État
    // ------------------------------------------------------------------
    private val prefs by lazy { getSharedPreferences("omra", Context.MODE_PRIVATE) }
    private var ecranActuel = -1 // -1 = accueil
    private val ar: Boolean get() = prefs.getString("langue", "fr") == "ar"

    /** Renvoie le texte dans la langue choisie. */
    private fun t(fr: String, arabe: String) = if (ar) arabe else fr

    private val vibreur: Vibrator? by lazy {
        if (Build.VERSION.SDK_INT >= 31) {
            (getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager)?.defaultVibrator
        } else {
            getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = cVert
        if (prefs.contains("langue")) afficherAccueil() else afficherChoixLangue()
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (ecranActuel >= 0) afficherAccueil() else super.onBackPressed()
    }

    private fun changerLangue(code: String) {
        prefs.edit().putString("langue", code).apply()
        if (ecranActuel >= 0 && ecranActuel < etapes.size) afficherEtape(ecranActuel) else afficherAccueil()
    }

    // ------------------------------------------------------------------
    // Outils d'interface
    // ------------------------------------------------------------------
    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()

    private fun fondArrondi(couleur: Int, rayon: Int = 16, bordure: Int? = null) = GradientDrawable().apply {
        cornerRadius = dp(rayon).toFloat()
        setColor(couleur)
        if (bordure != null) setStroke(dp(1), bordure)
    }

    private fun texte(s: String, taille: Float, couleur: Int = cTexte, gras: Boolean = false) =
        TextView(this).apply {
            text = s
            textSize = if (ar) taille + 1f else taille
            setTextColor(couleur)
            if (gras) typeface = Typeface.DEFAULT_BOLD
            setLineSpacing(0f, if (ar) 1.3f else 1.15f)
        }

    private fun bouton(s: String, couleur: Int, action: () -> Unit) = Button(this).apply {
        text = s
        textSize = 17f
        isAllCaps = false
        setTextColor(Color.WHITE)
        background = fondArrondi(couleur, 14)
        setPadding(dp(14), dp(14), dp(14), dp(14))
        setOnClickListener { action() }
    }

    private fun lpLargeur(haut: Int = 0, bas: Int = 0) = LinearLayout.LayoutParams(
        LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
    ).apply { topMargin = dp(haut); bottomMargin = dp(bas) }

    private fun nouvelleColonne(): Pair<ScrollView, LinearLayout> {
        val col = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(16), dp(16), dp(32))
        }
        val scroll = ScrollView(this).apply {
            setBackgroundColor(cFond)
            layoutDirection = if (ar) View.LAYOUT_DIRECTION_RTL else View.LAYOUT_DIRECTION_LTR
            addView(col)
        }
        return scroll to col
    }

    private fun vibrer(motif: LongArray) {
        val v = vibreur ?: return
        if (!v.hasVibrator()) return
        if (Build.VERSION.SDK_INT >= 26) {
            v.vibrate(VibrationEffect.createWaveform(motif, -1))
        } else {
            @Suppress("DEPRECATION")
            v.vibrate(motif, -1)
        }
    }

    private fun estFaite(i: Int) = prefs.getBoolean("fait_$i", false)

    private val MATCH = ViewGroupLp.MATCH_PARENT
    private val WRAP = ViewGroupLp.WRAP_CONTENT

    /** Identifiant d'une photo intégrée (0 si elle n'a pas pu être téléchargée à la compilation). */
    private fun idImage(nom: String): Int =
        if (nom.isEmpty()) 0 else resources.getIdentifier(nom, "drawable", packageName)

    private fun photo(nom: String, rayon: Int = 14): ImageView? {
        val id = idImage(nom)
        if (id == 0) return null
        return ImageView(this).apply {
            setImageResource(id)
            scaleType = ImageView.ScaleType.CENTER_CROP
            background = fondArrondi(Color.parseColor("#DDD6C4"), rayon)
            clipToOutline = true
        }
    }

    /** Grande photo avec un dégradé sombre et un titre par-dessus. */
    private fun banniere(nom: String, titre: String, sousTitre: String): FrameLayout {
        val cadre = FrameLayout(this).apply {
            background = fondArrondi(cVert, 20)
            clipToOutline = true
        }
        val id = idImage(nom)
        if (id != 0) {
            cadre.addView(ImageView(this).apply {
                setImageResource(id)
                scaleType = ImageView.ScaleType.CENTER_CROP
            }, FrameLayout.LayoutParams(MATCH, MATCH))
            cadre.addView(View(this).apply {
                background = GradientDrawable(
                    GradientDrawable.Orientation.TOP_BOTTOM,
                    intArrayOf(0x22000000, 0x33000000, 0xDD000000.toInt())
                )
            }, FrameLayout.LayoutParams(MATCH, MATCH))
        }
        val textes = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(12), dp(16), dp(16))
        }
        textes.addView(texte(titre, 26f, Color.WHITE, true))
        if (sousTitre.isNotEmpty()) textes.addView(texte(sousTitre, 14f, Color.parseColor("#F1EEDF")))
        cadre.addView(textes, FrameLayout.LayoutParams(MATCH, WRAP, Gravity.BOTTOM))
        return cadre
    }

    private fun ouvrirMaps(requete: String) {
        val uri = Uri.parse("https://www.google.com/maps/search/?api=1&query=" + Uri.encode(requete))
        try {
            startActivity(Intent(Intent.ACTION_VIEW, uri))
        } catch (e: ActivityNotFoundException) {
            AlertDialog.Builder(this)
                .setMessage(t("Aucune application de cartes n'est installée.", "لا يوجد تطبيق خرائط على الهاتف."))
                .setPositiveButton("OK", null)
                .show()
        }
    }

    private fun carteLieux(lieux: List<Lieu>): View {
        val carte = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            background = fondArrondi(cCarte, 14, Color.parseColor("#E4DCC6"))
            setPadding(dp(14), dp(12), dp(14), dp(12))
        }
        carte.addView(texte(t("📍 Lieux et itinéraires", "📍 الأماكن والخرائط"), 15f, cOr, true), lpLargeur(bas = 4))
        lieux.forEach { l ->
            val ligne = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                setPadding(0, dp(8), 0, dp(8))
            }
            val textes = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
            textes.addView(texte(t(l.nomFr, l.nomAr), 16f, cTexte, true))
            val note = t(l.noteFr, l.noteAr)
            if (note.isNotEmpty()) textes.addView(texte(note, 13f, cGris))
            ligne.addView(textes, LinearLayout.LayoutParams(0, WRAP, 1f))
            ligne.addView(Button(this).apply {
                text = t("Maps", "الخريطة")
                textSize = 14f
                isAllCaps = false
                setTextColor(Color.WHITE)
                background = fondArrondi(Color.parseColor("#1A73E8"), 12)
                setPadding(dp(14), dp(6), dp(14), dp(6))
                minHeight = 0
                minimumHeight = 0
                setOnClickListener { ouvrirMaps(l.requete) }
            }, LinearLayout.LayoutParams(WRAP, WRAP).apply { marginStart = dp(10) })
            carte.addView(ligne, lpLargeur())
        }
        return carte
    }

    private fun carteRepere(r: Repere): View {
        val carte = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            background = fondArrondi(cCarte, 14, Color.parseColor("#E4DCC6"))
            setPadding(dp(10), dp(10), dp(10), dp(12))
        }
        photo(r.image, 12)?.let { carte.addView(it, LinearLayout.LayoutParams(MATCH, dp(190))) }
        carte.addView(texte(t(r.titreFr, r.titreAr), 17f, cVert, true).apply {
            setPaddingRelative(dp(4), dp(8), dp(4), 0)
        })
        carte.addView(texte(t(r.texteFr, r.texteAr), 15f, cTexte).apply {
            setPaddingRelative(dp(4), dp(2), dp(4), 0)
        })
        return carte
    }

    // ------------------------------------------------------------------
    // Choix de la langue (premier lancement)
    // ------------------------------------------------------------------
    private fun afficherChoixLangue() {
        ecranActuel = -1
        val (scroll, col) = nouvelleColonne()
        col.gravity = Gravity.CENTER_HORIZONTAL
        col.addView(texte("🕋", 64f).apply { gravity = Gravity.CENTER }, lpLargeur(haut = 60))
        col.addView(texte("Mon Omra  ·  عمرتي", 28f, cVert, true).apply { gravity = Gravity.CENTER }, lpLargeur(haut = 8))
        col.addView(texte("Choisissez la langue\nاختر اللغة", 18f, cGris).apply { gravity = Gravity.CENTER }, lpLargeur(haut = 16, bas = 28))
        col.addView(bouton("العربية", cVert) { changerLangue("ar") }.apply { textSize = 22f }, lpLargeur(bas = 12))
        col.addView(bouton("Français", cVertClair) { changerLangue("fr") }.apply { textSize = 20f }, lpLargeur())
        setContentView(scroll)
    }

    // ------------------------------------------------------------------
    // Écran d'accueil
    // ------------------------------------------------------------------
    private fun afficherAccueil() {
        ecranActuel = -1
        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        val (scroll, col) = nouvelleColonne()

        // En-tête : photo de la Mosquée al-Haram
        val entete = banniere(
            "img_mosquee",
            "🕋  " + t("Mon Omra", "عمرتي"),
            "لَبَّيْكَ اللَّهُمَّ عُمْرَةً\n" + t("Guide pas à pas · Invocations · Compteurs", "دليل خطوة بخطوة · أدعية · عدّادات")
        )
        val langue = texte(t("العربية", "Français"), 15f, Color.WHITE, true).apply {
            background = fondArrondi(0xAA0E3B20.toInt(), 12)
            setPadding(dp(12), dp(6), dp(12), dp(6))
            isClickable = true
            setOnClickListener { changerLangue(if (ar) "fr" else "ar") }
        }
        entete.addView(langue, FrameLayout.LayoutParams(WRAP, WRAP, Gravity.TOP or Gravity.END).apply {
            topMargin = dp(12); marginEnd = dp(12)
        })
        col.addView(entete, LinearLayout.LayoutParams(MATCH, dp(240)).apply { bottomMargin = dp(14) })

        // Progression
        val nbFaites = etapes.indices.count { estFaite(it) }
        col.addView(texte(
            t("$nbFaites / ${etapes.size} étapes terminées", "أُنجزت $nbFaites من ${etapes.size} خطوات"),
            15f, cVert, true
        ), lpLargeur(bas = 6))
        col.addView(ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal).apply {
            max = etapes.size
            progress = nbFaites
        }, lpLargeur(bas = 12))

        // Liste des étapes
        etapes.forEachIndexed { i, e ->
            val fait = estFaite(i)
            val ligne = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                background = fondArrondi(cCarte, 14, if (fait) cVertClair else Color.parseColor("#E4DCC6"))
                setPadding(dp(14), dp(14), dp(14), dp(14))
                isClickable = true
                setOnClickListener { afficherEtape(i) }
            }
            // Vignette : photo de l'étape (ou emoji), avec le numéro en pastille
            val vignette = FrameLayout(this)
            val image = photo(imagesEtapes[i], 12)
            if (image != null) {
                vignette.addView(image, FrameLayout.LayoutParams(MATCH, MATCH))
            } else {
                vignette.addView(texte(e.emoji, 26f).apply {
                    gravity = Gravity.CENTER
                    background = fondArrondi(Color.parseColor("#EFE8D6"), 12)
                }, FrameLayout.LayoutParams(MATCH, MATCH))
            }
            vignette.addView(texte(if (fait) "✓" else "${i + 1}", 12f, Color.WHITE, true).apply {
                gravity = Gravity.CENTER
                background = fondArrondi(if (fait) cVertClair else cOr, 12)
            }, FrameLayout.LayoutParams(dp(24), dp(24), Gravity.BOTTOM or Gravity.END))
            ligne.addView(vignette, LinearLayout.LayoutParams(dp(64), dp(64)))
            val textes = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPaddingRelative(dp(12), 0, dp(4), 0)
            }
            textes.addView(texte(t(e.titreFr, e.titreAr), 17f, cTexte, true))
            textes.addView(texte(t(e.resumeFr, e.resumeAr), 14f, cGris))
            ligne.addView(textes, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
            ligne.addView(texte(t("›", "‹"), 26f, cGris))
            col.addView(ligne, lpLargeur(bas = 10))
        }

        col.addView(bouton(t("Recommencer une nouvelle Omra", "بدء عمرة جديدة"), Color.parseColor("#7A6A4F")) {
            AlertDialog.Builder(this)
                .setTitle(t("Recommencer ?", "إعادة البدء؟"))
                .setMessage(t(
                    "Toutes les étapes et les compteurs seront remis à zéro.",
                    "ستُعاد جميع الخطوات والعدادات إلى الصفر."
                ))
                .setPositiveButton(t("Oui", "نعم")) { _, _ ->
                    val langueChoisie = prefs.getString("langue", "fr")
                    prefs.edit().clear().putString("langue", langueChoisie).apply()
                    afficherAccueil()
                }
                .setNegativeButton(t("Non", "لا"), null)
                .show()
        }, lpLargeur(haut = 10))

        col.addView(texte(
            t(
                "Ce guide résume les rites selon l'avis courant, avec les principales différences de l'école malikite. En cas de doute, demandez à votre guide ou à un savant.",
                "يلخّص هذا الدليل المناسك على القول المشهور، مع أهم ما يخالف فيه المذهب المالكي. وعند الشك اسأل مرشدك أو أحد العلماء."
            ),
            12f, cGris
        ).apply { gravity = Gravity.CENTER }, lpLargeur(haut = 16))

        col.addView(texte(t("📷 Crédits photos", "📷 مصادر الصور"), 13f, cVertClair, true).apply {
            gravity = Gravity.CENTER
            setPadding(0, dp(8), 0, dp(8))
            isClickable = true
            setOnClickListener { afficherCredits() }
        }, lpLargeur(haut = 4))

        setContentView(scroll)
    }

    // ------------------------------------------------------------------
    // Crédits des photos (licences Creative Commons)
    // ------------------------------------------------------------------
    private fun afficherCredits() {
        ecranActuel = 100
        val (scroll, col) = nouvelleColonne()
        col.addView(texte(t("‹ Retour", "› رجوع"), 17f, cVert, true).apply {
            setPadding(0, dp(6), 0, dp(6))
            isClickable = true
            setOnClickListener { afficherAccueil() }
        }, lpLargeur(bas = 8))
        col.addView(texte(t("Crédits photos", "مصادر الصور"), 24f, cVert, true), lpLargeur(bas = 4))
        col.addView(texte(
            t("Photos issues de Wikimedia Commons, utilisées selon leur licence.", "الصور من ويكيميديا كومنز، مستعملة وفق تراخيصها."),
            14f, cGris
        ), lpLargeur(bas = 12))
        credits.forEach { c ->
            val ligne = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                background = fondArrondi(cCarte, 12, Color.parseColor("#E4DCC6"))
                setPadding(dp(10), dp(10), dp(10), dp(10))
            }
            photo(c.image, 10)?.let { ligne.addView(it, LinearLayout.LayoutParams(dp(56), dp(56))) }
            ligne.addView(texte("${c.titre}\n${c.auteur} · ${c.licence}", 13f, cTexte).apply {
                setPaddingRelative(dp(12), 0, 0, 0)
            }, LinearLayout.LayoutParams(0, WRAP, 1f))
            col.addView(ligne, lpLargeur(bas = 8))
        }
        setContentView(scroll)
    }

    // ------------------------------------------------------------------
    // Écran d'une étape
    // ------------------------------------------------------------------
    private fun afficherEtape(i: Int) {
        ecranActuel = i
        val e = etapes[i]
        if (e.compteur > 0) window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        else window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        val (scroll, col) = nouvelleColonne()

        // Barre du haut
        val barre = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        barre.addView(texte(t("‹ Étapes", "› الخطوات"), 17f, cVert, true).apply {
            setPaddingRelative(0, dp(6), dp(16), dp(6))
            isClickable = true
            setOnClickListener { afficherAccueil() }
        })
        barre.addView(View(this), LinearLayout.LayoutParams(0, 1, 1f))
        barre.addView(texte(t("Étape ${i + 1} / ${etapes.size}", "الخطوة ${i + 1} من ${etapes.size}"), 14f, cGris))
        col.addView(barre, lpLargeur(bas = 8))

        // Titre : grande photo si disponible, sinon simple titre
        if (idImage(imagesEtapes[i]) != 0) {
            col.addView(
                banniere(imagesEtapes[i], "${e.emoji}  ${t(e.titreFr, e.titreAr)}", t(e.resumeFr, e.resumeAr)),
                LinearLayout.LayoutParams(MATCH, dp(220)).apply { bottomMargin = dp(14) }
            )
        } else {
            col.addView(texte("${e.emoji}  ${t(e.titreFr, e.titreAr)}", 26f, cVert, true), lpLargeur(bas = 12))
        }

        // Consignes
        val consignes = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            background = fondArrondi(cCarte, 14, Color.parseColor("#E4DCC6"))
            setPadding(dp(14), dp(12), dp(14), dp(12))
        }
        consignes.addView(texte(t("À faire", "ما يجب فعله"), 15f, cOr, true), lpLargeur(bas = 4))
        (if (ar) e.consignesAr else e.consignesFr).forEach { c ->
            consignes.addView(texte("•  $c", 16f), lpLargeur(haut = 4))
        }
        col.addView(consignes, lpLargeur(bas = 14))

        // Compteurs
        when (e.compteur) {
            1 -> blocTawaf(col)
            2 -> blocSai(col)
        }

        // Repères en photo
        reperesEtapes[i]?.let { liste ->
            col.addView(texte(t("Repères à reconnaître", "معالم تعرّف عليها"), 20f, cVert, true), lpLargeur(haut = 4, bas = 8))
            liste.forEach { col.addView(carteRepere(it), lpLargeur(bas = 12)) }
        }

        // Lieux à ouvrir dans Maps
        lieuxEtapes[i]?.let { col.addView(carteLieux(it), lpLargeur(bas = 14)) }

        // Invocations
        col.addView(texte(t("Invocations", "الأدعية والأذكار"), 20f, cVert, true), lpLargeur(haut = 4, bas = 8))
        e.duas.forEach { col.addView(carteDua(it), lpLargeur(bas = 12)) }

        // Bas de page
        val fait = estFaite(i)
        col.addView(bouton(
            if (fait) t("Étape déjà terminée ✓ (suivante)", "الخطوة منجزة ✓ (التالية)")
            else t("Étape terminée ✓", "تمت الخطوة ✓"),
            cVertClair
        ) {
            prefs.edit().putBoolean("fait_$i", true).apply()
            vibrer(longArrayOf(0, 60))
            if (i + 1 < etapes.size) afficherEtape(i + 1) else afficherFin()
        }, lpLargeur(haut = 8))
        if (i > 0) {
            col.addView(bouton(t("‹ Étape précédente", "الخطوة السابقة ›"), Color.parseColor("#8A8A8A")) {
                afficherEtape(i - 1)
            }, lpLargeur(haut = 8))
        }

        setContentView(scroll)
        scroll.scrollTo(0, 0)
    }

    private fun carteDua(d: Dua): View {
        val carte = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            background = fondArrondi(cCarte, 14, cOr)
            setPadding(dp(14), dp(12), dp(14), dp(14))
        }
        carte.addView(texte(t(d.titreFr, d.titreAr), 15f, cOr, true), lpLargeur(bas = 6))
        if (d.arabe.isNotEmpty()) {
            carte.addView(texte(d.arabe, 25f, cTexte).apply {
                gravity = Gravity.CENTER
                textDirection = View.TEXT_DIRECTION_RTL
                setLineSpacing(0f, 1.35f)
            }, lpLargeur(bas = 8))
        }
        if (!ar) {
            if (d.translit.isNotEmpty()) {
                carte.addView(texte(d.translit, 15f, cVertClair).apply {
                    setTypeface(typeface, Typeface.ITALIC)
                }, lpLargeur(bas = 6))
            }
            if (d.sensFr.isNotEmpty()) carte.addView(texte(d.sensFr, 15f, cTexte))
        }
        val note = t(d.noteFr, d.noteAr)
        if (note.isNotEmpty()) {
            val principal = d.arabe.isEmpty()
            carte.addView(
                texte(if (principal) note else "ℹ️ $note", if (principal) 16f else 13f, if (principal) cTexte else cGris),
                lpLargeur(haut = 6)
            )
        }
        val source = t(d.sourceFr, d.sourceAr)
        if (source.isNotEmpty()) {
            carte.addView(texte(t("📖 Source : $source", "📖 المصدر: $source"), 12f, cOr), lpLargeur(haut = 6))
        }
        return carte
    }

    // ------------------------------------------------------------------
    // Compteur de Tawaf
    // ------------------------------------------------------------------
    private fun blocTawaf(col: LinearLayout) {
        val bloc = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            background = fondArrondi(cVert, 18)
            setPadding(dp(16), dp(16), dp(16), dp(16))
        }
        val titre = texte(t("Compteur de tours", "عدّاد الأشواط"), 15f, Color.parseColor("#D7E8DC"), true)
        val nombre = texte("", 64f, Color.WHITE, true).apply { gravity = Gravity.CENTER }
        val points = texte("", 26f, Color.parseColor("#F3D57A")).apply { gravity = Gravity.CENTER }
        val conseil = texte("", 15f, Color.WHITE).apply { gravity = Gravity.CENTER }
        lateinit var btn: Button
        val annuler = bouton(t("Annuler le dernier tour", "إلغاء آخر شوط"), Color.parseColor("#0E3B20")) {}

        fun maj() {
            val n = prefs.getInt("tawaf", 0)
            nombre.text = "$n / 7"
            points.text = "●".repeat(n) + "○".repeat(7 - n)
            conseil.text = when {
                n >= 7 -> t("✅ Tawaf terminé !\nAllez prier 2 rak'at au Maqam Ibrahim.", "✅ تم الطواف!\nتوجّه لصلاة ركعتين خلف المقام.")
                n < 3 -> t("Tour ${n + 1} en cours : raml (pas rapides) pour les hommes, si possible.", "الشوط ${n + 1}: الرَّمَل للرجال إن تيسر.")
                else -> t("Tour ${n + 1} en cours : marche normale.", "الشوط ${n + 1}: مشي عادي.")
            }
            btn.isEnabled = n < 7
            btn.alpha = if (n < 7) 1f else 0.4f
            btn.text = if (n < 7) t("Tour ${n + 1} terminé ✓", "انتهى الشوط ${n + 1} ✓") else t("7 tours faits", "اكتملت 7 أشواط")
            annuler.visibility = if (n > 0) View.VISIBLE else View.GONE
        }

        btn = bouton("", Color.parseColor("#D4A72C")) {
            val n = (prefs.getInt("tawaf", 0) + 1).coerceAtMost(7)
            prefs.edit().putInt("tawaf", n).apply()
            if (n == 7) vibrer(longArrayOf(0, 500, 200, 500)) else vibrer(longArrayOf(0, 120))
            maj()
        }.apply { textSize = 20f }
        annuler.setOnClickListener {
            val n = (prefs.getInt("tawaf", 0) - 1).coerceAtLeast(0)
            prefs.edit().putInt("tawaf", n).apply()
            maj()
        }

        bloc.addView(titre)
        bloc.addView(nombre)
        bloc.addView(points, lpLargeur(bas = 6))
        bloc.addView(conseil, lpLargeur(bas = 12))
        bloc.addView(btn, lpLargeur())
        bloc.addView(annuler, lpLargeur(haut = 8))
        maj()
        col.addView(bloc, lpLargeur(bas = 16))
    }

    // ------------------------------------------------------------------
    // Compteur de Sa'i
    // ------------------------------------------------------------------
    private fun blocSai(col: LinearLayout) {
        val bloc = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            background = fondArrondi(cVert, 18)
            setPadding(dp(16), dp(16), dp(16), dp(16))
        }
        val titre = texte(t("Compteur de trajets", "عدّاد أشواط السعي"), 15f, Color.parseColor("#D7E8DC"), true)
        val nombre = texte("", 64f, Color.WHITE, true).apply { gravity = Gravity.CENTER }
        val points = texte("", 26f, Color.parseColor("#F3D57A")).apply { gravity = Gravity.CENTER }
        val position = texte("", 18f, Color.WHITE, true).apply { gravity = Gravity.CENTER }
        lateinit var btn: Button
        val annuler = bouton(t("Annuler le dernier trajet", "إلغاء آخر شوط"), Color.parseColor("#0E3B20")) {}
        val safa = t("Safa", "الصفا")
        val marwa = t("Marwa", "المروة")

        fun maj() {
            val n = prefs.getInt("sai", 0)
            val ici = if (n % 2 == 0) safa else marwa
            val vers = if (n % 2 == 0) marwa else safa
            nombre.text = "$n / 7"
            points.text = "●".repeat(n) + "○".repeat(7 - n)
            position.text = if (n >= 7) t(
                "✅ Sa'i terminé à Marwa !\nPassez au rasage / raccourcissement.",
                "✅ تم السعي عند المروة!\nانتقل إلى الحلق أو التقصير."
            ) else t("Vous êtes à $ici  →  allez vers $vers", "أنت عند $ici  ←  اتجه إلى $vers")
            btn.isEnabled = n < 7
            btn.alpha = if (n < 7) 1f else 0.4f
            btn.text = if (n < 7) t("Je suis arrivé à $vers ✓", "وصلت إلى $vers ✓") else t("7 trajets faits", "اكتملت 7 أشواط")
            annuler.visibility = if (n > 0) View.VISIBLE else View.GONE
        }

        btn = bouton("", Color.parseColor("#D4A72C")) {
            val n = (prefs.getInt("sai", 0) + 1).coerceAtMost(7)
            prefs.edit().putInt("sai", n).apply()
            if (n == 7) vibrer(longArrayOf(0, 500, 200, 500)) else vibrer(longArrayOf(0, 120))
            maj()
        }.apply { textSize = 20f }
        annuler.setOnClickListener {
            val n = (prefs.getInt("sai", 0) - 1).coerceAtLeast(0)
            prefs.edit().putInt("sai", n).apply()
            maj()
        }

        bloc.addView(titre)
        bloc.addView(nombre)
        bloc.addView(points, lpLargeur(bas = 6))
        bloc.addView(position, lpLargeur(bas = 12))
        bloc.addView(btn, lpLargeur())
        bloc.addView(annuler, lpLargeur(haut = 8))
        maj()
        col.addView(bloc, lpLargeur(bas = 16))
    }

    // ------------------------------------------------------------------
    // Écran de fin
    // ------------------------------------------------------------------
    private fun afficherFin() {
        ecranActuel = etapes.size
        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        val (scroll, col) = nouvelleColonne()
        col.gravity = Gravity.CENTER_HORIZONTAL
        col.addView(texte("🕋", 64f).apply { gravity = Gravity.CENTER }, lpLargeur(haut = 40))
        col.addView(texte(t("Omra terminée", "تمّت العمرة"), 28f, cVert, true).apply { gravity = Gravity.CENTER }, lpLargeur(haut = 8))
        col.addView(texte("تَقَبَّلَ اللهُ مِنَّا وَمِنْكُمْ", 28f, cOr).apply {
            gravity = Gravity.CENTER
            textDirection = View.TEXT_DIRECTION_RTL
        }, lpLargeur(haut = 12))
        col.addView(texte(t("Qu'Allah accepte votre Omra.", "تقبّل الله عمرتك."), 17f, cTexte).apply { gravity = Gravity.CENTER }, lpLargeur(haut = 8, bas = 24))
        col.addView(bouton(t("Retour aux étapes", "العودة إلى الخطوات"), cVertClair) { afficherAccueil() }, lpLargeur())
        vibrer(longArrayOf(0, 300, 150, 300, 150, 300))
        setContentView(scroll)
    }
}
