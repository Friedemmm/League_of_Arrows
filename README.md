# 🏹 League of Arrows

Sistema de gestión de torneos de Tiro con Arco
---

## 📋 Requisitos

- [Docker](https://www.docker.com/) instalado y corriendo

Para verificar:
```bash
docker --version
docker compose version
```

---

## 🚀 Instalación y Despliegue

### Windows

**1. Descomprimir el archivo `.zip` y entrar a la carpeta por consola:**
```bash
cd League_of_Arrows-main
```

**2. Dentro de la carpeta habrá que levantar los contenedores**
```bash
docker compose up --build
```
> ⏱️ La primera vez tarda entre 3 y 6 minutos mientras descarga imágenes y compila el backend con Maven. Las siguientes veces es mucho más rápido.

**3. Abrir la aplicación**

| Servicio  | URL                        |
|-----------|----------------------------|
| Frontend  | http://localhost:80        |
| Backend   | http://localhost:8080/api  |

**4. Detener la aplicación**
```bash
# Detener sin borrar datos
docker compose down

# Detener Y borrar la base de datos (reset total)
docker compose down -v
```

---

### Linux

**1. Descomprimir el archivo `.zip` y entrar a la carpeta por consola:**
```bash
cd League_of_Arrows-main
```

**2. Verificar que Docker esté corriendo**
```bash
sudo systemctl status docker

# Si no está corriendo:
sudo systemctl start docker
```

**3. Asegurarse de que PostgreSQL local esté apagado** porque usa el mismo puerto y genera conflicto
```bash
sudo systemctl stop postgresql
```

**4. Levantar los contenedores**
```bash
sudo docker compose up --build
```

**5. Abrir la aplicación**

| Servicio  | URL                        |
|-----------|----------------------------|
| Frontend  | http://localhost:80        |
| Backend   | http://localhost:8080/api  |

**6. Detener la aplicación**
```bash
# Detener sin borrar datos
docker compose down

# Detener Y borrar la base de datos (reset total)
docker compose down -v
```

---

## 👤 Usuarios de Prueba

Todos los usuarios comparten la misma contraseña: **`admin123`**

### 🔑 Admin

| Campo      | Valor                        |
|------------|------------------------------|
| Email      | admin@leagueofarrows.com     |
| Contraseña | admin123                     |
| Acceso     | Todo el sistema (CRUD arqueros, torneos, inscripciones, registrar rondas) |

### 🏹 Arquero

| Campo      | Valor           |
|------------|-----------------|
| Email      | ashe@gmail.com  |
| Contraseña | admin123        |
| Acceso     | Solo su historial y ranking del mes |

**Otros arqueros disponibles** (misma contraseña):
- `varus@gmail.com`
- `kindred@gmail.com`
- `vayne@gmail.com`
- `quinn@gmail.com`
- `twitch@gmail.com`

---

## 🛠️ Comandos Útiles

```bash
# Ver logs de todos los servicios
docker compose logs -f

# Ver logs solo del backend
docker compose logs -f backend

# Ver logs solo de la base de datos
docker compose logs -f db

# Ver el estado de los contenedores
docker compose ps

# Abrir consola de PostgreSQL
docker exec -it loa_db psql -U loa_user -d league_of_arrows
```

---

## ❗ Posibles Errores

<details>
<summary><strong>Puerto 80 ocupado</strong></summary>

Otro programa está usando el puerto 80 (XAMPP, IIS, etc.).

**Solución:** Detenerlo, o cambiar el puerto en `docker-compose.yml`:
```yaml
ports:
  - "3000:80"   # cambiar 80 por otro puerto disponible
```
Luego abrir `http://localhost:3000`
</details>

<details>
<summary><strong>Puerto 5432 ocupado</strong></summary>

Hay un PostgreSQL instalado localmente corriendo.

**Solución:** Detener el servicio local, o cambiar el puerto en `docker-compose.yml` bajo el servicio `db`:
```yaml
ports:
  - "5433:5432"
```
</details>

<details>
<summary><strong>Puerto 8080 ocupado</strong></summary>

**Solución:** Cambiar el puerto en `docker-compose.yml` bajo el servicio `backend`:
```yaml
ports:
  - "8081:8080"
```
</details>

<details>
<summary><strong>"Cannot connect to the Docker daemon"</strong></summary>

Docker no está corriendo.

**Solución:** Abrir Docker Desktop y esperar que inicie, luego volver a ejecutar el comando.
</details>

<details>
<summary><strong>El backend se reinicia en loop</strong></summary>

Esto ocurre cuando el backend intenta conectarse antes de que la base de datos esté lista. Es normal al inicio.

Esperar unos segundos — el sistema se recupera solo. Si después de 2 minutos sigue fallando:
```bash
docker compose down -v
docker compose up --build
```
</details>

<details>
<summary><strong>Página en blanco o "Cannot GET /"</strong></summary>

El frontend todavía está compilando.

**Solución:** Esperar 30 segundos y recargar la página.
</details>

<details>
<summary><strong>Login dice "credenciales inválidas"</strong></summary>

El seeder no se ejecutó correctamente.

**Solución:** Resetear la base de datos:
```bash
docker compose down -v
docker compose up --build
```
Esto borra la BD y la recrea con los datos de prueba.
</details>

---

*© League of Arrows · Universidad de Santiago de Chile · 2026*
