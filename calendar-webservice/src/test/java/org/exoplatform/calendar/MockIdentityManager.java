package org.exoplatform.calendar;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.social.core.identity.IdentityProvider;
import org.exoplatform.social.core.identity.IdentityProviderPlugin;
import org.exoplatform.social.core.identity.SpaceMemberFilterListAccess;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.profile.ProfileFilter;
import org.exoplatform.social.core.profile.ProfileListener;
import org.exoplatform.social.core.profile.ProfileListenerPlugin;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.storage.api.IdentityStorage;
import org.exoplatform.webui.exception.MessageException;

public class MockIdentityManager implements IdentityManager {
    @Override
    public List<Identity> getLastIdentities(int i) {
        return null;
    }

    @Override
    public Identity getOrCreateIdentity(String s, String s1, boolean b) {
        return null;
    }

    @Override
    public Identity getIdentity(String s, boolean b) {
        return null;
    }

    @Override
    public Identity updateIdentity(Identity identity) {
        return null;
    }

    @Override
    public void deleteIdentity(Identity identity) {

    }

    @Override
    public void hardDeleteIdentity(Identity identity) {

    }

    @Override
    public ListAccess<Identity> getConnectionsWithListAccess(Identity identity) {
        return null;
    }

    @Override
    public Profile getProfile(Identity identity) {
        return null;
    }

    @Override
    public InputStream getAvatarInputStream(Identity identity) throws IOException {
        return null;
    }

    @Override
    public InputStream getBannerInputStream(Identity identity) throws IOException {
        return null;
    }

    @Override
    public void updateProfile(Profile profile) throws MessageException {

    }

    @Override
    public ListAccess<Identity> getIdentitiesByProfileFilter(String s, ProfileFilter profileFilter, boolean b) {
        return null;
    }

    @Override
    public ListAccess<Identity> getIdentitiesForUnifiedSearch(String s, ProfileFilter profileFilter) {
        return null;
    }

    @Override
    public ListAccess<Identity> getSpaceIdentityByProfileFilter(Space space, ProfileFilter profileFilter, SpaceMemberFilterListAccess.Type type, boolean b) {
        return null;
    }

    @Override
    public void addIdentityProvider(IdentityProvider<?> identityProvider) {

    }

    @Override
    public void removeIdentityProvider(IdentityProvider<?> identityProvider) {

    }

    @Override
    public void registerProfileListener(ProfileListenerPlugin profileListenerPlugin) {

    }

    @Override
    public void registerIdentityProviders(IdentityProviderPlugin identityProviderPlugin) {

    }

    @Override
    public void processEnabledIdentity(String s, boolean b) {

    }

    @Override
    public Identity getIdentity(String s) {
        return null;
    }

    @Override
    public Identity getOrCreateIdentity(String s, String s1) {
        return null;
    }

    @Override
    public List<Identity> getIdentitiesByProfileFilter(String s, ProfileFilter profileFilter) throws Exception {
        return null;
    }

    @Override
    public List<Identity> getIdentitiesByProfileFilter(String s, ProfileFilter profileFilter, long l, long l1) throws Exception {
        return null;
    }

    @Override
    public List<Identity> getIdentitiesByProfileFilter(ProfileFilter profileFilter) throws Exception {
        return null;
    }

    @Override
    public List<Identity> getIdentitiesByProfileFilter(ProfileFilter profileFilter, long l, long l1) throws Exception {
        return null;
    }

    @Override
    public List<Identity> getIdentitiesFilterByAlphaBet(String s, ProfileFilter profileFilter) throws Exception {
        return null;
    }

    @Override
    public List<Identity> getIdentitiesFilterByAlphaBet(String s, ProfileFilter profileFilter, long l, long l1) throws Exception {
        return null;
    }

    @Override
    public List<Identity> getIdentitiesFilterByAlphaBet(ProfileFilter profileFilter) throws Exception {
        return null;
    }

    @Override
    public Identity getIdentity(String s, String s1, boolean b) {
        return null;
    }

    @Override
    public long getIdentitiesCount(String s) {
        return 0;
    }

    @Override
    public boolean identityExisted(String s, String s1) {
        return false;
    }

    @Override
    public void saveIdentity(Identity identity) {

    }

    @Override
    public void saveProfile(Profile profile) {

    }

    @Override
    public void addOrModifyProfileProperties(Profile profile) throws Exception {

    }

    @Override
    public void updateAvatar(Profile profile) throws MessageException {

    }

    @Override
    public void updateBasicInfo(Profile profile) throws Exception {

    }

    @Override
    public void updateContactSection(Profile profile) throws Exception {

    }

    @Override
    public void updateExperienceSection(Profile profile) throws Exception {

    }

    @Override
    public void updateHeaderSection(Profile profile) throws Exception {

    }

    @Override
    public List<Identity> getIdentities(String s) throws Exception {
        return null;
    }

    @Override
    public List<Identity> getIdentities(String s, boolean b) throws Exception {
        return null;
    }

    @Override
    public List<Identity> getConnections(Identity identity) throws Exception {
        return null;
    }

    @Override
    public IdentityStorage getIdentityStorage() {
        return null;
    }

    @Override
    public IdentityStorage getStorage() {
        return null;
    }

    @Override
    public void registerProfileListener(ProfileListener profileListener) {

    }

    @Override
    public void unregisterProfileListener(ProfileListener profileListener) {

    }

    @Override
    public void addProfileListener(ProfileListenerPlugin profileListenerPlugin) {

    }

    @Override
    public List<String> sortIdentities(List<String> list, String s) {
        return null;
    }
}
