package com.quarkdown.stdlib.localization

import com.quarkdown.core.localization.Locale
import com.quarkdown.core.localization.LocaleLoader
import com.quarkdown.core.localization.LocalizationTable

/**
 * @param name name of the locale, e.g. `English`
 * @return the system locale named [name]
 */
private fun locale(name: String): Locale = LocaleLoader.SYSTEM.find(name) ?: error("Could not find locale \"$name\".")

/**
 * The stdlib's own localization table, registered in the context.
 */
internal val STDLIB_LOCALIZATION_TABLE: LocalizationTable by lazy {
    mapOf(
        locale("Chinese") to
            mapOf(
                "bibliography" to "参考文献",
                "error" to "错误",
                "figure" to "图",
                "important" to "重要",
                "listing" to "代码",
                "note" to "注意",
                "section" to "章节",
                "table" to "表",
                "tableofcontents" to "目录",
                "tableofcontents/docs" to "本页目录",
                "tip" to "提示",
                "todo" to "待办",
                "warning" to "警告",
            ),
        locale("English") to
            mapOf(
                "bibliography" to "References",
                "error" to "Error",
                "figure" to "Figure",
                "important" to "Important",
                "listing" to "Listing",
                "note" to "Note",
                "section" to "Section",
                "table" to "Table",
                "tableofcontents" to "Table of Contents",
                "tableofcontents/docs" to "On this page",
                "tip" to "Tip",
                "todo" to "To do",
                "warning" to "Warning",
            ),
        locale("French") to
            mapOf(
                "bibliography" to "Sources",
                "error" to "Erreur",
                "figure" to "Figure",
                "important" to "Important",
                "listing" to "Listing",
                "note" to "Note",
                "section" to "Section",
                "table" to "Tableau",
                "tableofcontents" to "Table des matières",
                "tableofcontents/docs" to "Sur cette page",
                "tip" to "Astuce",
                "todo" to "À faire",
                "warning" to "Attention",
            ),
        locale("German") to
            mapOf(
                "bibliography" to "Literaturverzeichnis",
                "error" to "Fehler",
                "figure" to "Abbildung",
                "important" to "Wichtig",
                "listing" to "Listing",
                "note" to "Hinweis",
                "section" to "Abschnitt",
                "table" to "Tabelle",
                "tableofcontents" to "Inhaltsverzeichnis",
                "tableofcontents/docs" to "Auf dieser Seite",
                "tip" to "Tip",
                "todo" to "Aufgabe",
                "warning" to "Warnung",
            ),
        locale("Italian") to
            mapOf(
                "bibliography" to "Riferimenti",
                "error" to "Errore",
                "figure" to "Figura",
                "important" to "Importante",
                "listing" to "Listato",
                "note" to "Nota",
                "section" to "Sezione",
                "table" to "Tabella",
                "tableofcontents" to "Indice",
                "tableofcontents/docs" to "In questa pagina",
                "tip" to "Consiglio",
                "todo" to "Da fare",
                "warning" to "Attenzione",
            ),
        locale("Japanese") to
            mapOf(
                "bibliography" to "参考文献",
                "error" to "エラー",
                "figure" to "図",
                "important" to "重要",
                "listing" to "リスト",
                "note" to "ノート",
                "section" to "セクション",
                "table" to "テーブル",
                "tableofcontents" to "目次",
                "tableofcontents/docs" to "このページの内容",
                "tip" to "ヒント",
                "todo" to "タスク",
                "warning" to "警告",
            ),
        locale("Polish") to
            mapOf(
                "bibliography" to "Bibliografia",
                "error" to "Błąd",
                "figure" to "Rysunek",
                "important" to "Ważne",
                "listing" to "Listing",
                "note" to "Notatka",
                "section" to "Sekcja",
                "table" to "Tabela",
                "tableofcontents" to "Spis treści",
                "tableofcontents/docs" to "Na tej stronie",
                "tip" to "Wskazówka",
                "todo" to "Do zrobienia",
                "warning" to "Ostrzeżenie",
            ),
        locale("Portuguese") to
            mapOf(
                "bibliography" to "Bibliografia",
                "error" to "Erro",
                "figure" to "Figura",
                "important" to "Importante",
                "listing" to "Listagem",
                "note" to "Nota",
                "section" to "Seção",
                "table" to "Tabela",
                "tableofcontents" to "Índice",
                "tableofcontents/docs" to "Nesta página",
                "tip" to "Dica",
                "todo" to "A fazer",
                "warning" to "Aviso",
            ),
        locale("Russian") to
            mapOf(
                "bibliography" to "Библиография",
                "error" to "Ошибка",
                "figure" to "Рисунок",
                "important" to "Важно",
                "listing" to "Listing",
                "note" to "Примечание",
                "section" to "Раздел",
                "table" to "Таблица",
                "tableofcontents" to "Содержание",
                "tableofcontents/docs" to "На этой странице",
                "tip" to "Совет",
                "todo" to "К выполнению",
                "warning" to "Предупреждение",
            ),
        locale("Ukrainian") to
            mapOf(
                "bibliography" to "Бібліографія",
                "error" to "Помилка",
                "figure" to "Рисунок",
                "important" to "Важливо",
                "listing" to "Listing",
                "note" to "Примітка",
                "section" to "Розділ",
                "table" to "Таблиця",
                "tableofcontents" to "Зміст",
                "tableofcontents/docs" to "На цій сторінці",
                "tip" to "Порада",
                "todo" to "До роботи",
                "warning" to "Попередження",
            ),
    )
}
