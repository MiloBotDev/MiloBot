package api.dnd5e;

import api.dnd5e.models.*;
import org.jetbrains.annotations.NotNull;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Optional;

@SuppressWarnings("unchecked")
public class DndApi {

	private final String baseUrl = "https://www.dnd5eapi.co/api";

	public DndApi() {
	}

	public static void main(String[] args) {
		Monster monster = new DndApi().getMonster("aboleth").get();
		System.out.println(monster);
	}

	/**
	 * Get a monster by its name.
	 *
	 * @param name The name of the monster.
	 * @return The monster as an Optional of {@link Monster} object.
	 */
	public Optional<Monster> getMonster(@NotNull String name) {
		Optional<JSONObject> request = request(Endpoints.MONSTER_BY_NAME, name.toLowerCase(Locale.ROOT));
		if (request.isPresent()) {

			JSONObject monsterAsJson = request.get();
			Monster monster = new Monster();
			if (monsterAsJson.get("index") != null) {
				monster.setIndex((String) monsterAsJson.get("index"));
			}
			if (monsterAsJson.get("name") != null) {
				monster.setName((String) monsterAsJson.get("name"));
			}
			if (monsterAsJson.get("url") != null) {
				monster.setUrl((String) monsterAsJson.get("url"));
			}
			if (monsterAsJson.get("charisma") != null) {
				monster.setCharisma((long) monsterAsJson.get("charisma"));
			}
			if (monsterAsJson.get("constitution") != null) {
				monster.setConstitution((long) monsterAsJson.get("constitution"));
			}
			if (monsterAsJson.get("dexterity") != null) {
				monster.setDexterity((long) monsterAsJson.get("dexterity"));
			}
			if (monsterAsJson.get("intelligence") != null) {
				monster.setIntelligence((long) monsterAsJson.get("intelligence"));
			}
			if (monsterAsJson.get("strength") != null) {
				monster.setStrength((long) monsterAsJson.get("strength"));
			}
			if (monsterAsJson.get("wisdom") != null) {
				monster.setWisdom((long) monsterAsJson.get("wisdom"));
			}
			if (monsterAsJson.get("actions") != null) {
				ArrayList<Action> actions = new ArrayList<>();
				for (Object o : (ArrayList<Object>) monsterAsJson.get("actions")) {
					Action action = new Action();
					JSONObject o1 = (JSONObject) o;
					if (o1.get("name") != null) {
						action.setName((String) o1.get("name"));
					}
					if (o1.get("desc") != null) {
						action.setDescription((String) o1.get("desc"));
					}
					Object options = o1.get("options");
					if (options != null) {
						Option option = new Option();
						Object choose = ((JSONObject) options).get("choose");
						if (choose != null) {
							option.setChoose((long) choose);
						}
						Object type = ((JSONObject) options).get("type");
						if (type != null) {
							option.setType((String) type);
						}
						Object from = ((JSONObject) options).get("from");
						if (from != null) {
							ArrayList<Choice> choices = new ArrayList<>();
							int counter = 0;
							for (Object o2 : (ArrayList<Object>) from) {
								JSONObject o3 = (JSONObject) o2;
								JSONObject jsonObject = (JSONObject) o3.get(String.valueOf(counter));
								if (jsonObject != null) {
									Choice choice = new Choice();
									choice.setIndex(String.valueOf(counter));
									if (jsonObject.get("name") != null) {
										choice.setName((String) jsonObject.get("name"));
										choice.setCount((long) jsonObject.get("count"));
									}
									choices.add(choice);
								}
							}
							option.setFrom(choices);
						}
						action.setOptions(option);
						if (o1.get("attack_bonus") != null) {
							action.setAttackBonus((long) o1.get("attack_bonus"));
						}
						if (o1.get("dc") != null) {
							Dice dice = new Dice();
							JSONObject dc = (JSONObject) o1.get("dc");
							if (dc.get("dc_type") != null) {
								DiceType diceType = new DiceType();
								JSONObject dcType = (JSONObject) dc.get("dc_type");
								if (dcType.get("index") != null) {
									diceType.setName((String) dcType.get("index"));
								}
								if (dcType.get("name") != null) {
									diceType.setName((String) dcType.get("name"));
								}
								if (dcType.get("url") != null) {
									diceType.setUrl((String) dcType.get("url"));
								}
								dice.setDiceType(diceType);
							}
							if (dc.get("dc_value") != null) {
								dice.setDiceValue((long) dc.get("dc_value"));
							}
							if (dc.get("success_type") != null) {
								dice.setSuccessType((String) dc.get("success_type"));
							}
						}
					}
					actions.add(action);
				}
				monster.setActions(actions);
			}
			return Optional.of(monster);
		} else {
			return Optional.empty();
		}
	}

	/**
	 * Makes a request to the API and returns the response as an Optional of JSONObject.
	 *
	 * @param endpoint The endpoint to request.
	 *                 Example: /monsters.
	 * @param param    The parameter to pass to the endpoint.
	 *                 (e.g. the name of the monster to request).
	 * @return The response as an Optional of JSONObject.
	 */
	private Optional<JSONObject> request(String endpoint, String param) {
		StringBuilder responseContent = new StringBuilder();
		HttpURLConnection conn;
		URL url;
		BufferedReader reader;
		String line;
		try {
			url = new URL(String.format("%s%s", baseUrl, String.format(Endpoints.MONSTER_BY_NAME, param)));
			conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			conn.setConnectTimeout(5000);
			conn.setReadTimeout(5000);
			int status = conn.getResponseCode();
			if (status == 200) {
				reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
				while ((line = reader.readLine()) != null) {
					responseContent.append(line);
				}
				JSONParser parser = new JSONParser();
				JSONObject json = (JSONObject) parser.parse(responseContent.toString());
				return Optional.of(json);
			} else {
				return Optional.empty();
			}
		} catch (IOException | ParseException e) {
			e.printStackTrace();
			return Optional.empty();
		}
	}
}
