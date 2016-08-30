package org.jenkinsci.plugins.database.sqlite.GlobalSQLiteDatabase

def f = namespace(lib.FormTagLib)

f.entry(field:"path",title:_("File Path")) {
    f.textbox()
}
