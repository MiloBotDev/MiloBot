package io.github.milobotdev.milobot.models;

/**
 * A record that represents a user's name and tag. This is used for caching a user's name and tag for faster access.
 *
 * @param userName The user's name.
 * @param id The user's discriminator.
 */
public record UserNameTag(String userName, short id) {

}
