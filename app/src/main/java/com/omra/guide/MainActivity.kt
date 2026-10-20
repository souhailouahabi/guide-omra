package com.omra.guide

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.ScrollView
import android.widget.TextView

/**
 * Mon Omra : guide pas à pas des rites de la Omra, avec invocations
 * (arabe, phonétique, français) et compteurs de Tawaf et de Sa'i.
 * Fonctionne entièrement hors ligne.
 */
class MainActivity : Activity() {

    // ------------------------------------------------------------------
    // Contenu
    // ------------------------------------------------------------------
    private data class Dua(
        val titre: String,
        val arabe: String,
        val translit: String,
        val sens: String,
        val note: String = ""
    )

    private data class Etape(
        val titre: String,
        val emoji: String,
        val resume: String,
        val consignes: List<String>,
        val duas: List<Dua>,
        val compteur: Int = 0 // 0 = aucun, 1 = Tawaf, 2 = Sa'i
    )

    private val talbiya = Dua(
        "Talbiya",
        "لَبَّيْكَ اللَّهُمَّ لَبَّيْكَ، لَبَّيْكَ لَا شَرِيكَ لَكَ لَبَّيْكَ، إِنَّ الْحَمْدَ وَالنِّعْمَةَ لَكَ وَالْمُلْكَ، لَا شَرِيكَ لَكَ",
        "Labbayka Allâhumma labbayk, labbayka lâ sharîka laka labbayk, inna l-hamda wa n-ni'mata laka wa l-mulk, lâ sharîka lak.",
        "Me voici, ô Allah, me voici. Me voici, Tu n'as pas d'associé, me voici. La louange, le bienfait et la royauté T'appartiennent. Tu n'as pas d'associé.",
        "À répéter souvent depuis le Miqat jusqu'au début du Tawaf. Hommes à voix haute, femmes à voix basse."
    )

    private val etapes = listOf(
        Etape(
            "L'Ihram", "🤍", "Se préparer et entrer en état de sacralisation",
            listOf(
                "Avant le Miqat : faire le ghusl (grand lavage), couper ongles et poils si besoin.",
                "Hommes : parfum sur le corps (pas sur les vêtements), puis deux pièces blanches non cousues (izar et rida) et des sandales.",
                "Femmes : vêtements habituels couvrants, sans niqab, sans gants et sans parfum.",
                "Au Miqat (en avion : à l'annonce du passage du Miqat), formulez l'intention de la Omra.",
                "Commencez la Talbiya et répétez-la jusqu'au début du Tawaf."
            ),
            listOf(
                Dua("Intention (au Miqat)", "لَبَّيْكَ عُمْرَةً", "Labbayka 'umratan.", "Me voici, (répondant à Ton appel) pour une Omra."),
                Dua(
                    "Condition (facultative)", "اللَّهُمَّ مَحِلِّي حَيْثُ حَبَسْتَنِي",
                    "Allâhumma mahillî haythu habastanî.",
                    "Ô Allah, je me désacralise là où Tu m'empêches de continuer.",
                    "Pour celui qui craint un empêchement (maladie, etc.)."
                ),
                talbiya
            )
        ),
        Etape(
            "Les interdits de l'Ihram", "⛔", "Ce qu'il faut éviter jusqu'à la fin",
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
            listOf(talbiya)
        ),
        Etape(
            "Entrée à la Mosquée al-Haram", "🕌", "Entrer et se préparer au Tawaf",
            listOf(
                "Entrez du pied droit en disant l'invocation d'entrée.",
                "Soyez en état d'ablutions (wudû') : elles sont exigées pour le Tawaf.",
                "Hommes : faites l'idtibâ' (épaule droite découverte, le rida passé sous l'aisselle droite) pendant tout le Tawaf.",
                "Arrêtez la Talbiya au moment de commencer le Tawaf."
            ),
            listOf(
                Dua(
                    "Entrée à la mosquée (pied droit)",
                    "بِسْمِ اللهِ، وَالصَّلَاةُ وَالسَّلَامُ عَلَى رَسُولِ اللهِ، اللَّهُمَّ افْتَحْ لِي أَبْوَابَ رَحْمَتِكَ",
                    "Bismillâh, wa s-salâtu wa s-salâmu 'alâ rasûlillâh. Allâhumma ftah lî abwâba rahmatik.",
                    "Au nom d'Allah, que la prière et la paix soient sur le Messager d'Allah. Ô Allah, ouvre-moi les portes de Ta miséricorde."
                ),
                Dua(
                    "Protection en entrant",
                    "أَعُوذُ بِاللهِ الْعَظِيمِ، وَبِوَجْهِهِ الْكَرِيمِ، وَسُلْطَانِهِ الْقَدِيمِ، مِنَ الشَّيْطَانِ الرَّجِيمِ",
                    "A'ûdhu billâhi l-'azîm, wa bi-wajhihi l-karîm, wa sultânihi l-qadîm, mina sh-shaytâni r-rajîm.",
                    "Je cherche refuge auprès d'Allah l'Immense, auprès de Son noble Visage et de Son autorité éternelle, contre Satan le maudit."
                )
            )
        ),
        Etape(
            "Le Tawaf", "🕋", "7 tours autour de la Kaaba",
            listOf(
                "Commencez à la ligne de la Pierre noire, la Kaaba à votre gauche.",
                "7 tours complets : chaque tour se termine en revenant à la Pierre noire.",
                "Hommes : raml (petits pas rapides) pendant les 3 premiers tours, si possible.",
                "Touchez le coin yéménite de la main si c'est facile, sans le baiser ni faire de signe.",
                "Appuyez sur le bouton vert à la fin de chaque tour."
            ),
            listOf(
                Dua(
                    "À la Pierre noire (début de chaque tour)", "بِسْمِ اللهِ، وَاللهُ أَكْبَرُ",
                    "Bismillâh, wa Llâhu akbar.",
                    "Au nom d'Allah, et Allah est le plus grand.",
                    "Touchez ou embrassez la Pierre si c'est facile, sinon faites un signe de la main droite sans la baiser. Aux tours suivants, « Allâhu akbar » suffit."
                ),
                Dua(
                    "Entre le coin yéménite et la Pierre noire",
                    "رَبَّنَا آتِنَا فِي الدُّنْيَا حَسَنَةً وَفِي الْآخِرَةِ حَسَنَةً وَقِنَا عَذَابَ النَّارِ",
                    "Rabbanâ âtinâ fi d-dunyâ hasanatan wa fi l-âkhirati hasanatan wa qinâ 'adhâba n-nâr.",
                    "Seigneur, accorde-nous une belle part ici-bas, une belle part dans l'au-delà, et protège-nous du châtiment du Feu. (2:201)"
                ),
                Dua(
                    "Pendant le reste du tour", "", "",
                    "Aucune invocation précise n'est fixée pour chaque tour : invoquez librement, dans votre langue, faites du dhikr ou lisez le Coran."
                )
            ),
            compteur = 1
        ),
        Etape(
            "Prière au Maqam Ibrahim", "🤲", "2 rak'at après le Tawaf",
            listOf(
                "Hommes : couvrez de nouveau l'épaule droite.",
                "Priez 2 rak'at derrière le Maqam Ibrahim si possible, sinon n'importe où dans la mosquée.",
                "Ne gênez pas les personnes qui font le Tawaf."
            ),
            listOf(
                Dua(
                    "En allant vers le Maqam Ibrahim", "وَاتَّخِذُوا مِنْ مَقَامِ إِبْرَاهِيمَ مُصَلًّى",
                    "Wattakhidhû min maqâmi Ibrâhîma musallâ.",
                    "Et prenez la station d'Ibrahim pour lieu de prière. (2:125)"
                ),
                Dua(
                    "Les 2 rak'at", "", "",
                    "1ʳᵉ rak'a : al-Fâtiha puis sourate al-Kâfirûn (109).\n2ᵉ rak'a : al-Fâtiha puis sourate al-Ikhlâs (112)."
                )
            )
        ),
        Etape(
            "L'eau de Zamzam", "💧", "Boire et invoquer",
            listOf(
                "Buvez de l'eau de Zamzam en invoquant Allah pour ce que vous souhaitez.",
                "Si c'est facile, retournez toucher la Pierre noire avant le Sa'i.",
                "Dirigez-vous ensuite vers le mont Safa."
            ),
            listOf(
                Dua(
                    "En buvant Zamzam", "مَاءُ زَمْزَمَ لِمَا شُرِبَ لَهُ",
                    "Mâ'u Zamzama limâ shuriba lah.",
                    "L'eau de Zamzam est pour ce pour quoi on la boit. (hadith, Ibn Mâja)",
                    "Faites les invocations de votre choix en la buvant."
                )
            )
        ),
        Etape(
            "Le Sa'i", "⛰️", "7 trajets entre Safa et Marwa",
            listOf(
                "7 trajets : Safa → Marwa = 1, Marwa → Safa = 2… On termine à Marwa.",
                "Sur Safa et sur Marwa, faites face à la Kaaba, levez les mains et invoquez.",
                "Hommes : trottinez entre les deux lumières vertes.",
                "Les ablutions sont recommandées mais pas obligatoires pour le Sa'i.",
                "Appuyez sur le bouton vert à chaque arrivée sur une colline."
            ),
            listOf(
                Dua(
                    "En approchant de Safa (1ʳᵉ fois seulement)",
                    "إِنَّ الصَّفَا وَالْمَرْوَةَ مِنْ شَعَائِرِ اللَّهِ\nأَبْدَأُ بِمَا بَدَأَ اللَّهُ بِهِ",
                    "Inna s-Safâ wa l-Marwata min sha'â'iri Llâh.\nAbda'u bimâ bada'a Llâhu bih.",
                    "Safa et Marwa sont vraiment parmi les rites d'Allah. (2:158)\nJe commence par ce par quoi Allah a commencé."
                ),
                Dua(
                    "Sur Safa et sur Marwa (face à la Kaaba, mains levées)",
                    "اللهُ أَكْبَرُ، اللهُ أَكْبَرُ، اللهُ أَكْبَرُ\nلَا إِلَهَ إِلَّا اللهُ وَحْدَهُ لَا شَرِيكَ لَهُ، لَهُ الْمُلْكُ وَلَهُ الْحَمْدُ، وَهُوَ عَلَى كُلِّ شَيْءٍ قَدِيرٌ، لَا إِلَهَ إِلَّا اللهُ وَحْدَهُ، أَنْجَزَ وَعْدَهُ، وَنَصَرَ عَبْدَهُ، وَهَزَمَ الْأَحْزَابَ وَحْدَهُ",
                    "Allâhu akbar (×3). Lâ ilâha illa Llâhu wahdahu lâ sharîka lah, lahu l-mulku wa lahu l-hamd, wa huwa 'alâ kulli shay'in qadîr. Lâ ilâha illa Llâhu wahdah, anjaza wa'dah, wa nasara 'abdah, wa hazama l-ahzâba wahdah.",
                    "Allah est le plus grand (×3). Il n'y a de divinité qu'Allah, Seul, sans associé. À Lui la royauté et la louange, et Il est capable de toute chose. Il n'y a de divinité qu'Allah, Seul. Il a tenu Sa promesse, secouru Son serviteur et vaincu Seul les coalisés.",
                    "À dire 3 fois, avec des invocations personnelles entre chaque fois."
                ),
                Dua(
                    "Entre les lumières vertes",
                    "رَبِّ اغْفِرْ وَارْحَمْ، إِنَّكَ أَنْتَ الْأَعَزُّ الْأَكْرَمُ",
                    "Rabbi ghfir warham, innaka anta l-a'azzu l-akram.",
                    "Seigneur, pardonne et fais miséricorde, Tu es certes le Plus Puissant, le Plus Généreux.",
                    "Rapportée de Compagnons (Ibn Mas'ûd, Ibn 'Umar). Vous pouvez aussi invoquer librement."
                )
            ),
            compteur = 2
        ),
        Etape(
            "Rasage ou raccourcissement", "✂️", "Fin de la Omra",
            listOf(
                "Hommes : se raser la tête (le meilleur) ou raccourcir les cheveux sur toute la tête.",
                "Femmes : couper l'équivalent d'un bout de doigt (environ 2 cm) à l'extrémité des cheveux.",
                "Votre Omra est terminée : les interdits de l'Ihram sont levés."
            ),
            listOf(
                Dua(
                    "Pour féliciter", "تَقَبَّلَ اللهُ مِنَّا وَمِنْكُمْ",
                    "Taqabbala Llâhu minnâ wa minkum.",
                    "Qu'Allah accepte de nous et de vous."
                )
            )
        )
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
        afficherAccueil()
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (ecranActuel >= 0) afficherAccueil() else super.onBackPressed()
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
            textSize = taille
            setTextColor(couleur)
            if (gras) typeface = Typeface.DEFAULT_BOLD
            setLineSpacing(0f, 1.15f)
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

    // ------------------------------------------------------------------
    // Écran d'accueil
    // ------------------------------------------------------------------
    private fun afficherAccueil() {
        ecranActuel = -1
        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        val (scroll, col) = nouvelleColonne()

        // En-tête
        val entete = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            background = fondArrondi(cVert, 20)
            setPadding(dp(16), dp(22), dp(16), dp(20))
        }
        entete.addView(texte("🕋", 44f).apply { gravity = Gravity.CENTER })
        entete.addView(texte("Mon Omra", 28f, Color.WHITE, true).apply { gravity = Gravity.CENTER })
        entete.addView(texte("لَبَّيْكَ اللَّهُمَّ عُمْرَةً", 22f, Color.parseColor("#F3D57A")).apply {
            gravity = Gravity.CENTER
            textDirection = View.TEXT_DIRECTION_RTL
        })
        entete.addView(texte("Guide pas à pas · Invocations · Compteurs", 14f, Color.parseColor("#D7E8DC")).apply {
            gravity = Gravity.CENTER
            setPadding(0, dp(6), 0, 0)
        })
        col.addView(entete, lpLargeur(bas = 14))

        // Progression
        val nbFaites = etapes.indices.count { estFaite(it) }
        col.addView(texte("$nbFaites / ${etapes.size} étapes terminées", 15f, cVert, true), lpLargeur(bas = 6))
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
            val pastille = texte(if (fait) "✓" else "${i + 1}", 18f, Color.WHITE, true).apply {
                gravity = Gravity.CENTER
                background = fondArrondi(if (fait) cVertClair else cOr, 20)
            }
            ligne.addView(pastille, LinearLayout.LayoutParams(dp(40), dp(40)))
            val textes = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(dp(12), 0, 0, 0)
            }
            textes.addView(texte("${e.emoji}  ${e.titre}", 17f, cTexte, true))
            textes.addView(texte(e.resume, 14f, cGris))
            ligne.addView(textes, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
            ligne.addView(texte("›", 26f, cGris))
            col.addView(ligne, lpLargeur(bas = 10))
        }

        col.addView(bouton("Recommencer une nouvelle Omra", Color.parseColor("#7A6A4F")) {
            AlertDialog.Builder(this)
                .setTitle("Recommencer ?")
                .setMessage("Toutes les étapes et les compteurs seront remis à zéro.")
                .setPositiveButton("Oui") { _, _ ->
                    prefs.edit().clear().apply()
                    afficherAccueil()
                }
                .setNegativeButton("Non", null)
                .show()
        }, lpLargeur(haut = 10))

        col.addView(texte(
            "Ce guide résume les rites selon l'avis courant. En cas de doute, demandez à votre guide ou à un savant.",
            12f, cGris
        ).apply { gravity = Gravity.CENTER }, lpLargeur(haut = 16))

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
        barre.addView(texte("‹ Étapes", 17f, cVert, true).apply {
            setPadding(0, dp(6), dp(16), dp(6))
            isClickable = true
            setOnClickListener { afficherAccueil() }
        })
        barre.addView(View(this), LinearLayout.LayoutParams(0, 1, 1f))
        barre.addView(texte("Étape ${i + 1} / ${etapes.size}", 14f, cGris))
        col.addView(barre, lpLargeur(bas = 8))

        col.addView(texte("${e.emoji}  ${e.titre}", 26f, cVert, true), lpLargeur(bas = 12))

        // Consignes
        val consignes = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            background = fondArrondi(cCarte, 14, Color.parseColor("#E4DCC6"))
            setPadding(dp(14), dp(12), dp(14), dp(12))
        }
        consignes.addView(texte("À faire", 15f, cOr, true), lpLargeur(bas = 4))
        e.consignes.forEach { c ->
            consignes.addView(texte("•  $c", 16f), lpLargeur(haut = 4))
        }
        col.addView(consignes, lpLargeur(bas = 14))

        // Compteurs
        when (e.compteur) {
            1 -> blocTawaf(col)
            2 -> blocSai(col)
        }

        // Invocations
        col.addView(texte("Invocations", 20f, cVert, true), lpLargeur(haut = 4, bas = 8))
        e.duas.forEach { col.addView(carteDua(it), lpLargeur(bas = 12)) }

        // Bas de page
        val fait = estFaite(i)
        col.addView(bouton(if (fait) "Étape déjà terminée ✓ (suivante)" else "Étape terminée ✓", cVertClair) {
            prefs.edit().putBoolean("fait_$i", true).apply()
            vibrer(longArrayOf(0, 60))
            if (i + 1 < etapes.size) afficherEtape(i + 1) else afficherFin()
        }, lpLargeur(haut = 8))
        if (i > 0) {
            col.addView(bouton("‹ Étape précédente", Color.parseColor("#8A8A8A")) {
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
        carte.addView(texte(d.titre, 15f, cOr, true), lpLargeur(bas = 6))
        if (d.arabe.isNotEmpty()) {
            carte.addView(texte(d.arabe, 25f, cTexte).apply {
                gravity = Gravity.CENTER
                textDirection = View.TEXT_DIRECTION_RTL
                setLineSpacing(0f, 1.35f)
            }, lpLargeur(bas = 8))
        }
        if (d.translit.isNotEmpty()) {
            carte.addView(texte(d.translit, 15f, cVertClair).apply {
                setTypeface(typeface, Typeface.ITALIC)
            }, lpLargeur(bas = 6))
        }
        carte.addView(texte(d.sens, 15f, cTexte))
        if (d.note.isNotEmpty()) {
            carte.addView(texte("ℹ️ ${d.note}", 13f, cGris), lpLargeur(haut = 6))
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
        val titre = texte("Compteur de tours", 15f, Color.parseColor("#D7E8DC"), true)
        val nombre = texte("", 64f, Color.WHITE, true).apply { gravity = Gravity.CENTER }
        val points = texte("", 26f, Color.parseColor("#F3D57A")).apply { gravity = Gravity.CENTER }
        val conseil = texte("", 15f, Color.WHITE).apply { gravity = Gravity.CENTER }
        lateinit var btn: Button
        val annuler = bouton("Annuler le dernier tour", Color.parseColor("#0E3B20")) {}

        fun maj() {
            val n = prefs.getInt("tawaf", 0)
            nombre.text = "$n / 7"
            points.text = "●".repeat(n) + "○".repeat(7 - n)
            conseil.text = when {
                n >= 7 -> "✅ Tawaf terminé !\nAllez prier 2 rak'at au Maqam Ibrahim."
                n < 3 -> "Tour ${n + 1} en cours : raml (pas rapides) pour les hommes, si possible."
                else -> "Tour ${n + 1} en cours : marche normale."
            }
            btn.isEnabled = n < 7
            btn.alpha = if (n < 7) 1f else 0.4f
            btn.text = if (n < 7) "Tour ${n + 1} terminé ✓" else "7 tours faits"
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
        val titre = texte("Compteur de trajets", 15f, Color.parseColor("#D7E8DC"), true)
        val nombre = texte("", 64f, Color.WHITE, true).apply { gravity = Gravity.CENTER }
        val points = texte("", 26f, Color.parseColor("#F3D57A")).apply { gravity = Gravity.CENTER }
        val position = texte("", 18f, Color.WHITE, true).apply { gravity = Gravity.CENTER }
        lateinit var btn: Button
        val annuler = bouton("Annuler le dernier trajet", Color.parseColor("#0E3B20")) {}

        fun maj() {
            val n = prefs.getInt("sai", 0)
            val ici = if (n % 2 == 0) "Safa" else "Marwa"
            val vers = if (n % 2 == 0) "Marwa" else "Safa"
            nombre.text = "$n / 7"
            points.text = "●".repeat(n) + "○".repeat(7 - n)
            position.text = if (n >= 7) "✅ Sa'i terminé à Marwa !\nPassez au rasage / raccourcissement."
            else "Vous êtes à $ici  →  allez vers $vers"
            btn.isEnabled = n < 7
            btn.alpha = if (n < 7) 1f else 0.4f
            btn.text = if (n < 7) "Je suis arrivé à $vers ✓" else "7 trajets faits"
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
        col.addView(texte("Omra terminée", 28f, cVert, true).apply { gravity = Gravity.CENTER }, lpLargeur(haut = 8))
        col.addView(texte("تَقَبَّلَ اللهُ مِنَّا وَمِنْكُمْ", 28f, cOr).apply {
            gravity = Gravity.CENTER
            textDirection = View.TEXT_DIRECTION_RTL
        }, lpLargeur(haut = 12))
        col.addView(texte("Qu'Allah accepte votre Omra.", 17f, cTexte).apply { gravity = Gravity.CENTER }, lpLargeur(haut = 8, bas = 24))
        col.addView(bouton("Retour aux étapes", cVertClair) { afficherAccueil() }, lpLargeur())
        vibrer(longArrayOf(0, 300, 150, 300, 150, 300))
        setContentView(scroll)
    }
}
