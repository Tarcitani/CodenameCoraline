# Ferramentas de Apoio (Tools) — Codename Coraline

Este diretório contém ferramentas reproduzíveis em Python e Java para geração de arte, atlas de texturas, gravuras do Bestiário, molduras de barras de vida e validação de modelos offline.

Nenhum asset visual deve ser desenhado "na mão" sem um script gerador correspondente versionado aqui.

---

## 1. Categorias de Ferramentas

### 1.1 Geradores de Texturas e Atlas (`generate_<mob>_assets.py`)
Scripts em Python (utilizando a biblioteca `Pillow`) responsáveis por criar as texturas das entidades, respeitando o estilo Blockbench/Minecraft 16x16, com mapeamento UV sem sobreposição e camadas emissivas alinhadas:
- `generate_baby_dragon_assets.py` / `generate_dragon_remodel.py`: texturas e anatomias do Dragão Companheiro (Bebê, Jovem, Adulto).
- `generate_basilisk_assets.py`: atlas base e emissivo do Basilisco da Praga.
- `generate_lithophage_assets.py`: textura rochosa e cristais de âmbar do Litófago.
- `generate_last_gift_collector_assets.py`: texturas do Cobrador e do seu Trenó.
- `generate_canopy_breaker_assets.py` / `generate_bark_macaque_assets.py`: texturas do Quebra-Copas e dos Micos de Casca.
- `generate_crimson_commander_assets.py`: armadura carmesim e espada larga do Comandante.
- `generate_ten_threads_witch_assets.py`: texturas da Matriarca e das 10 marionetes.

### 1.2 Exportadores de Geometria Java (`Export<Mob>Model.java`)
Classes Java executáveis que instanciam a hierarquia real de `ModelPart` da entidade e exportam os vértices, normais e coordenadas de faces para arquivos temporários. Isso permite que os scripts de gravura desenhem as criaturas a partir do modelo real:
- `ExportBabyDragonModel.java`
- `ExportBasiliskModel.java`
- `ExportLithophageModel.java`
- `ExportLastGiftCollectorModel.java`
- `ExportCanopyBreakerModel.java`
- `ExportCrimsonCommanderModel.java`
- `ExportTenThreadsWitchModel.java`

### 1.3 Geradores de Gravuras do Bestiário (`generate_<mob>_sketches.py`)
Scripts que processam os dados exportados pelos modelos Java e geram as pranchas em estilo sépia/tinta para o livro do Bestiário:
- Dimensões padronizadas:
  - **Anatomia:** `112×80` px
  - **Combate:** `112×72` px
  - **Espólios / Itens:** `112×64` px
- Executáveis também via tarefas Gradle dedicadas (`*-sketches.gradle`).

### 1.4 Geradores de Boss Bars Temáticas (`generate_boss_bar_<boss>.py`)
Scripts Python que desenham as molduras ornamentadas das barras de vida salvas em `src/main/resources/assets/codenamecoraline/textures/gui/boss_bar/<id>.png`:
- `generate_boss_bar_last_gift_collector.py`
- `generate_boss_bar_canopy_breaker.py`
- `generate_boss_bar_crimson_commander.py`
- `generate_boss_bar_ten_threads_witch.py`
- Geometria e coordenadas de corte devem coincidir rigorosamente com a implementação do estilo em `client/bossbar/ThemedBossBars.java`.

### 1.5 Prévias e Diagnósticos
- `dragon-evolution-preview.gradle` / `dragon-observer-preview.gradle`: tarefas de inicialização isolada de cliente para inspeção rápida de modelos e montaria.
- `generate_canopy_animation_bible.py` / `generate_crimson_animation_table.py`: geradores de tabelas de timing e poses de referência para documentação.

---

## 2. Como Executar os Scripts

### Requisitos
- Python 3.10+
- Pillow (`pip install Pillow`)

### Exemplo de Uso
```bash
# Gerar ou atualizar texturas de uma criatura:
python tools/generate_crimson_commander_assets.py

# Gerar moldura da boss bar:
python tools/generate_boss_bar_crimson_commander.py

# Exportar gravuras do Bestiário via Gradle:
./gradlew -b tools/crimson-commander-sketches.gradle
```

