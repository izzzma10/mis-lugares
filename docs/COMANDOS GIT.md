COMANDOS DE GIT PARA TRABAJAR ENTRE RAMAS 

*------1. Crear y moverte de rama------*

*--Crear una rama nueva y moverte a ella inmediatamente:--*

git switch -c nombre-de-tu-rama

*---Cambiar a una rama existente:---*

git switch nombre-de-tu-rama

*---Volver rápidamente a la última rama en la que estabas:---*

git switch -

*------2. Ver en qué Rama estás y Estado------*

*---Listar tus ramas locales (la que tiene un * es en la que estás actualmente):---*

git branch

*---Listar todas las ramas (incluyendo las remotas en GitHub/GitLab):---*

git branch -a

*---Ver qué archivos has modificado antes de cambiar de rama:---*

git status

*------3. ¿Qué hago con mis cambios si quiero cambiar de rama sin hacer commit?------*

*---Guardar temporalmente tus cambios en progreso:---*

git stash


*---Cambiarte de rama de forma limpia:---*

git switch otra-rama

*---(Al regresar a tu rama original) Recuperar tus cambios guardados:---*

git stash pop

*------4. Traer Cambios y Fusionar (Merge)------*

*---Cámbiate a la rama que va a recibir los cambios:---*

git switch main

*---Trae lo último del servidor (buena práctica):---*

git pull origin main


*---Fusiona tu rama secundaria en main:---*

git merge feature/login

*------5. Subir y Borrar Ramas------*

*---Subir tu rama nueva al servidor remoto (GitHub/GitLab) por primera vez:---*

git push -u origin nombre-de-tu-rama
(Después de hacer esto la primera vez, solo necesitarás escribir git push).

*---Eliminar una rama local (una vez que ya la fusionaste y no la necesitas):---*

git branch -d nombre-de-tu-rama

*---Forzar el borrado de una rama local (si decides descartar el trabajo sin fusionar):---*

*---Eliminar la rama en el servidor remoto:---*

git push origin --delete nombre-de-tu-rama